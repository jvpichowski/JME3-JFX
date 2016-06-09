package com.jme3.jfx;

import com.jme3.app.Application;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.util.BufferUtils;
import com.sun.glass.ui.Pixels;
import com.sun.javafx.embed.AbstractEvents;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.stage.EmbeddedWindow;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

/**
 * Implementation
 *
 * Copies Fx View to Texture
 *
 * Created by jan on 30.05.16.
 */
final class FxContainer {

    private final int width;
    private final int height;

    //not intended to change
    private Context context;
    private JFxManager jfxManager;
    private Application application;
    private String name = "";

    //creation livecycle and focus management is handled by this class
    private boolean isCreated = false;
    private boolean focus;

    //Describe PixelFormat for color transition between Java Fx and jME
    private CompletableFuture<Image.Format> nativeFormat = new CompletableFuture<Image.Format>();
    private int	alphaByteOffset = 3;


    //buffers for swapping image data from Java Fx to jME
    private ByteBuffer jmeData;
    private ByteBuffer fxData;
    private Image jmeImage;
    private Texture2D texture;
    private final Semaphore imageExchange = new Semaphore(1);
    private boolean	fxDataReady = false;


    private HostInterface hostContainer;
    protected EmbeddedSceneInterface scenePeer;
    //needed for focus management
    protected EmbeddedStageInterface stagePeer;
    protected volatile EmbeddedWindow stage;
    volatile Scene scene;

    //**********************************************************//
    // Texture Getter                                           //
    //**********************************************************//

    /**
     * Realy needed or different way?
     * @return
     */
    public int getAlphaByteOffset(){
        return alphaByteOffset;
    }
    public Image getImage(){
        return jmeImage;
    }

    public Texture2D getTexture(){
        return texture;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getName(){
        return name;
    }


    public final Context getContext() {
        return context;
    }


    public int getWidth() {
        return width;
    }


    public int getHeight() {
        return height;
    }


    public FxContainer(int width, int height){
        this.width = width;
        this.height = height;
    }



    //**********************************************************//
    // Livecircle Management                                    //
    //**********************************************************//

    public void create(Context context) {
        if(isCreated()){
            throw new IllegalStateException("Context is already created!");
        }
        this.context = context;
        this.jfxManager = context.getJFxManager();
        this.application = jfxManager.getApplication();
        jfxManager.enqueue(() -> {
            switch (Pixels.getNativeFormat()) {
                case Pixels.Format.BYTE_ARGB:
                    nativeFormat.complete(Image.Format.ARGB8);
                    alphaByteOffset = 0;
                    break;
                case Pixels.Format.BYTE_BGRA_PRE:
                    nativeFormat.complete(Image.Format.BGRA8);
                    alphaByteOffset = 3;
                    break;
                default:
                    nativeFormat.complete(Image.Format.ARGB8);
                    alphaByteOffset = 0;
                    break;
            };
        });
        hostContainer = new FxHost(new FxHost.FxContainerBridge() {
            @Override
            public void setStagePeer(EmbeddedStageInterface embeddedStage) {
                stagePeer = embeddedStage;
            }

            @Override
            public EmbeddedStageInterface getStagePeer() {
                return stagePeer;
            }

            @Override
            public int getWidth() {
                return FxContainer.this.getWidth();
            }

            @Override
            public int getHeight() {
                return FxContainer.this.getHeight();
            }

            @Override
            public EmbeddedSceneInterface getSceneInterface() {
                return scenePeer;
            }

            @Override
            public void setSceneInterface(EmbeddedSceneInterface embeddedScene) {
                scenePeer = embeddedScene;
            }

            @Override
            public void repaint() {
                FxContainer.this.repaint();
            }
        });
        try {
            this.jmeData = BufferUtils.createByteBuffer(getWidth() * getHeight() * 4);
            this.fxData = BufferUtils.createByteBuffer(getWidth() * getHeight() * 4);
            this.jmeImage = new Image(nativeFormat.get(), getWidth(), getHeight(), this.jmeData, ColorSpace.Linear);
            this.texture = new Texture2D(this.jmeImage);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
        isCreated = true;
    }

    private void repaint() {
        if (this.scenePeer == null) {
            return;
        }

        final boolean lock = this.imageExchange.tryAcquire();
        if (!lock) {
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    FxContainer.this.repaint();
                }
            });
            return;
        }
        try {

            final ByteBuffer data = this.fxData;
            data.clear();

            final IntBuffer buf = data.asIntBuffer();

            if (!this.scenePeer.getPixels(buf, getWidth(), getHeight())) {
                return;
            }

            data.flip();
            data.limit(getWidth() * getHeight() * 4);
            this.fxDataReady = true;

        } catch (final Exception exc) {
            exc.printStackTrace();
        } finally {
            this.imageExchange.release();
        }
        this.application.enqueue(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                final boolean updateImage = imageExchange.tryAcquire();
                // we update only if we can do that in nonblocking mode
                // if would need to block, it means that another callable with
                // newer data will be
                // enqueued soon, so we can just ignore this repaint
                if (updateImage) {
                    try {
                        if (fxDataReady) {
                            fxDataReady = false;
                            final ByteBuffer tmp = jmeData;
                            jmeData = fxData;
                            fxData = tmp;
                        }
                    } finally {
                        imageExchange.release();
                    }
                    jmeImage.setData(jmeData);
                } else {
                    // System.out.println("Skipping update due to contention");
                }
                return null;
            }
        });

    }


    public void destroy() {
        if(!isCreated()){
            return;
        }

        if (this.texture != null) {
            this.texture.setImage(null);
        }
        if (this.jmeImage != null) {
            this.jmeImage.dispose();
        }
        if (this.jmeData != null) {
            BufferUtils.destroyDirectBuffer(this.jmeData);
        }
        if (this.fxData != null) {
            BufferUtils.destroyDirectBuffer(this.fxData);
        }
        isCreated = false;
    }


    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }


    public final void setEnabled(boolean enabled) {

    }


    public final boolean isCreated() {
        return isCreated;
    }

    public final boolean isActive(){
        return scenePeer != null;
    }

    //**********************************************************//
    // Input Management                                         //
    //**********************************************************//

    /*
     * A notification about mouse event received by host container.
     * This method could be called from any thread and will be delivered
     * in the Java Fx Thread.
     */
    public void mouseEvent(int type, int button,
                           boolean primaryBtnDown, boolean middleBtnDown, boolean secondaryBtnDown,
                           int x, int y, int xAbs, int yAbs,
                           boolean shift, boolean ctrl, boolean alt, boolean meta,
                           int wheelRotation, boolean popupTrigger){

        jfxManager.enqueue(() -> scenePeer.mouseEvent(type, button,
                    primaryBtnDown, middleBtnDown, secondaryBtnDown,
                    x, y, xAbs, yAbs,
                    shift, ctrl, alt, meta,
                    wheelRotation, popupTrigger));
    }
    /*
     * A notification about key event received by host container.
     * This method could be called from any thread and will be delivered
     * in the Java Fx Thread.
     */
    public void keyEvent(int type, int key, char[] chars, int modifiers){
        jfxManager.enqueue(() -> scenePeer.keyEvent(type, key, chars, modifiers));
    }


    //**********************************************************//
    // Scene Management                                         //
    //**********************************************************//

    public void setScene(final Scene newScene) {
        this.setScene(newScene, newScene.getRoot());
    }

    protected void setScene(final Scene newScene, final Parent highLevelGroup) {
//		this.rootNode = highLevelGroup;
        jfxManager.enqueue(() -> setSceneImpl(newScene));
    }

	/*
	 * Called on JavaFX application thread.
	 */

    protected void setSceneImpl(final Scene newScene) {
        if (this.stage != null && newScene == null) {
            this.stage.hide();
            this.stage = null;
        }

        this.scene = newScene;
        if (this.stage == null && newScene != null) {
            this.stage = new EmbeddedWindow(this.hostContainer);
        }
        if (this.stage != null) {
            this.stage.setScene(newScene);
            if (!this.stage.isShowing()) {
                this.stage.show();
            }
        }
    }


    //**********************************************************//
    // Focus Management                                         //
    //**********************************************************//


    public final boolean hasFocus() {
        return focus;
    }


    public void grabFocus() {
        if (!focus && this.stagePeer != null) {
            stagePeer.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
            this.focus = true;
        }
    }


    public void loseFocus() {
        if (focus && this.stagePeer != null) {
            this.stagePeer.setFocused(false, AbstractEvents.FOCUSEVENT_DEACTIVATED);
            this.focus = false;
        }
    }
}
