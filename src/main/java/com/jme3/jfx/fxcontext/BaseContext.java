package com.jme3.jfx.fxcontext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import com.jme3.app.Application;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Stage;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3.jfx.fxcontext.input.InputAdapter;
import com.sun.glass.ui.Pixels;
import com.sun.istack.internal.NotNull;
import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.embed.AbstractEvents;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.stage.EmbeddedWindow;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Need to pass -Dprism.dirtyopts=false on startup
 *
 * @author abies / Artur Biesiadowski
 */

public abstract class BaseContext implements FxContext {

    protected JFxManager jfxManager;
	protected Application application;
	private boolean isCreated = false;
    private Stage primaryStage = new StageImpl();
    private String name = "";

    //needed for input
	protected EmbeddedStageInterface				stagePeer;
	protected EmbeddedSceneInterface				scenePeer;
    protected boolean								focus;


	protected volatile EmbeddedWindow	stage;
	HostInterface						hostContainer;
	protected InputAdapter inputAdapter;
	private final int width; //height of stage
	private final int height; //width of stage
	volatile Scene						scene;
	private Image								jmeImage;
	private Texture2D texture;
	private ByteBuffer							jmeData;
	private int	alphaByteOffset = 3;
	private ByteBuffer							fxData;
	private boolean								fxDataReady			= false;
	private CompletableFuture<Format>			nativeFormat		= new CompletableFuture<Format>();
//	CursorDisplayProvider				cursorDisplayProvider;
//	private Parent						rootNode;


    protected int getAlphaByteOffset(){
        return alphaByteOffset;
    }
    protected Texture2D getTexture(){
        return texture;
    }

	protected BaseContext(InputAdapter inputAdapter, int width, int height){
        this.width = width;
        this.height = height;
		this.inputAdapter = inputAdapter;

	}

    @Override public Image getImage(){
        return jmeImage;
    }

	@Override public EmbeddedSceneInterface getSceneInterface() { return scenePeer; };

	@Override public InputAdapter getInputAdapter() {
		return this.inputAdapter;
	}


//	public Scene getScene() {
//		return this.scene;
//	}


    @Override
	public Stage getStage(){
        return primaryStage;
	}

	@Override public final void setName(@NotNull  String name) {
        this.name = name;
	}

    @Override public final String getName(){
        return name;
    }

	@Override
	public boolean isCreated() {
		return isCreated;
	}

	@Override
	public void create(JFxManager jfxManager) {
        if(isCreated()){
            throw new IllegalStateException("Context is already created!");
        }
		this.jfxManager = jfxManager;
		this.application = jfxManager.getApplication();
        this.inputAdapter.setContext(this);
		jfxManager.enqueue(() -> {
			// TODO 3.1: use Format.ARGB8 and Format.BGRA8 and remove used of exchangeData, fx2jme_ARGB82ABGR8,...
			switch (Pixels.getNativeFormat()) {
				case Pixels.Format.BYTE_ARGB:
					nativeFormat.complete(Format.ARGB8);
					alphaByteOffset = 0;
					break;
				case Pixels.Format.BYTE_BGRA_PRE:
					nativeFormat.complete(Format.BGRA8);
					alphaByteOffset = 3;
					break;
				default:
					nativeFormat.complete(Format.ARGB8);
					alphaByteOffset = 0;
					break;
			};
		});
        hostContainer = new HostInterfaceImpl();
        try {
            this.jmeData = BufferUtils.createByteBuffer(getStage().getWidth() * getStage().getHeight() * 4);
            this.fxData = BufferUtils.createByteBuffer(getStage().getWidth() * getStage().getHeight() * 4);
            this.jmeImage = new Image(nativeFormat.get(), getStage().getWidth(), getStage().getHeight(), this.jmeData);
            this.texture = new Texture2D(this.jmeImage);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
        isCreated = true;
	}

	@Override
	public void restart() {

	}

	@Override
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

//	public EmbeddedWindow getStage() {
//		return this.stage;
//	}

	@Override
	public JFxManager getJFxManager(){
		return jfxManager;
	}

	@Override
	public Application getApplication() {
		return application;
	}

	public void setScene(final Scene newScene) {
		this.setScene(newScene, newScene.getRoot());
	}

	protected void setScene(final Scene newScene, final Parent highLevelGroup) {
//		this.rootNode = highLevelGroup;
        getJFxManager().enqueue(() -> setSceneImpl(newScene));
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

//		sceneContainerMap.put(this.stage, this);
	}

	protected final Semaphore	imageExchange	= new Semaphore(1);
//	public CursorType			lastcursor;

	private void paintComponent() {
		if (this.scenePeer == null) {
			return;
		}

		final boolean lock = this.imageExchange.tryAcquire();
		if (!lock) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					BaseContext.this.paintComponent();
				}
			});
			return;
		}
		try {

			final ByteBuffer data = this.fxData;
			data.clear();

			final IntBuffer buf = data.asIntBuffer();

			if (!this.scenePeer.getPixels(buf, this.width, this.height)) {
				return;
			}

//			if (this.fullscreenSupport) {
//				for (final PopupSnapper ps : this.activeSnappers) {
//					ps.paint(buf, this.width, this.height);
//				}
//			}

			data.flip();
			data.limit(this.width * this.height * 4);
			this.fxDataReady = true;

		} catch (final Exception exc) {
			exc.printStackTrace();
		} finally {
			this.imageExchange.release();
		}
		this.application.enqueue(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				final boolean updateImage = BaseContext.this.imageExchange.tryAcquire();
				// we update only if we can do that in nonblocking mode
				// if would need to block, it means that another callable with
				// newer data will be
				// enqueued soon, so we can just ignore this repaint
				if (updateImage) {
					try {
						if (BaseContext.this.fxDataReady) {
							BaseContext.this.fxDataReady = false;
							final ByteBuffer tmp = BaseContext.this.jmeData;
							BaseContext.this.jmeData = BaseContext.this.fxData;
							BaseContext.this.fxData = tmp;
						}
					} finally {
						BaseContext.this.imageExchange.release();
					}
					BaseContext.this.jmeImage.setData(BaseContext.this.jmeData);
				} else {
					// System.out.println("Skipping update due to contention");
				}
				return null;
			}
		});

	}

//	boolean[] mouseButtonState = new boolean[3];

	@Override
	public boolean hasFocus(){
		return focus;
	}

	@Override
	public void grabFocus() {
		if (!this.focus && this.stagePeer != null) {
			this.stagePeer.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
			this.focus = true;
		}
	}

	@Override
	public void loseFocus() {
		if (this.focus && this.stagePeer != null) {
			this.stagePeer.setFocused(false, AbstractEvents.FOCUSEVENT_DEACTIVATED);
			this.focus = false;
		}
	}


	private class HostInterfaceImpl implements HostInterface {
		@Override
		public void setEmbeddedStage(final EmbeddedStageInterface embeddedStage) {
            BaseContext.this.stagePeer = embeddedStage;
			if (BaseContext.this.stagePeer == null) {
				return;
			}
			if (getStage().getWidth() > 0 && getStage().getHeight() > 0) {
                BaseContext.this.stagePeer.setSize(getStage().getWidth(), getStage().getHeight());
			}

            BaseContext.this.stagePeer.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
		}

		@Override
		public void setEmbeddedScene(final EmbeddedSceneInterface embeddedScene) {
			BaseContext.this.scenePeer = embeddedScene;
			if (getSceneInterface() == null) {
				return;
			}

			// 8_u60 and later fix
			try {
				final Method scaler = embeddedScene.getClass().getMethod("setPixelScaleFactor", float.class);
				scaler.setAccessible(true);
				scaler.invoke(embeddedScene, 1f);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}

			if (getStage().getWidth() > 0 && getStage().getHeight() > 0) {
				getSceneInterface().setSize(getStage().getWidth(), getStage().getHeight());
			}

		}

		@Override
		public boolean requestFocus() {
			return true;
		}

		@Override
		public boolean traverseFocusOut(final boolean forward) {
			System.out.println("Called traverseFocusOut("+forward+")");
			return true;
		}

		@Override
		public void setPreferredSize(final int width, final int height) {
		}


		@Override
		public void repaint() {
			paintComponent();
		}

		@Override
		public void setEnabled(final boolean enabled) {
			//this.fxcontext.setFxEnabled(enabled);
		}

		@Override
		public void setCursor(final CursorFrame cursorFrame) {
//		if (this.fxcontext.cursorDisplayProvider != null) {
//			this.fxcontext.cursorDisplayProvider.showCursor(cursorFrame);
//		}
		}

		/**
		 * Grabs focus on this window.
		 * <p>
		 * All mouse clicks that occur in this window's client area or client-areas
		 * of any of its unfocusable owned windows are delivered as usual. Whenever
		 * a click occurs on another app's window (not related via the ownership
		 * relation with this one, or a focusable owned window), or on non-client
		 * area of any window (titlebar, etc.), or any third-party app's window, or
		 * native OS GUI (e.g. a taskbar), the grab is automatically reset, and the
		 * window that held the grab receives the FOCUS_UNGRAB event.
		 * <p>
		 * Note that for this functionality to work correctly, the window must have
		 * a focus upon calling this method. All owned popup windows that should be
		 * operable during the grabbed focus state (e.g. nested popup menus) must
		 * be unfocusable (see {@link #setFocusable}). Clicking a focusable owned
		 * window will reset the grab due to a focus transfer.
		 * <p>
		 * The click that occurs in another window and causes resetting of the grab
		 * may or may not be delivered to that other window depending on the native
		 * OS behavior.
		 * <p>
		 * If any of the application's windows already holds the grab, it is reset
		 * prior to grabbing the focus for this window. The method may be called
		 * multiple times for one window. Subsequent calls do not affect the grab
		 * status unless it is reset between the calls, in which case the focus
		 * is grabbed again.
		 * <p>
		 * Note that grabbing the focus on an application window may prevent
		 * delivering certain events to other applications until the grab is reset.
		 * Therefore, if the application has finished showing popup windows based
		 * on a user action (e.g. clicking a menu item), and doesn't require the
		 * grab any more, it should call the {@link #ungrabFocus} method. The
		 * FOCUS_UNGRAB event signals that the grab has been reset.
		 * <p>
		 * A user event handler associated with a menu item must be invoked after
		 * resetting the grab. Otherwise, if a developer debugs the application and
		 * has installed a breakpoint in the event handler, the debugger may become
		 * unoperable due to events blocking for other applications on some
		 * platforms.
		 *
		 * @return {@code true} if the operation is successful
		 * @throws IllegalStateException if the window isn't focused currently
		 */
		@Override
		public boolean grabFocus() {
			return true;
		}

		/**
		 * Manually ungrabs focus grabbed on this window previously.
		 * <p>
		 * This method resets the grab, and forces sending of the FOCUS_UNGRAB
		 * event. It should be used when popup windows (such as menus) should be
		 * dismissed manually, e.g. when a user clicks a menu item which usually
		 * causes the menus to hide.
		 *
		 * @see #grabFocus
		 */
		@Override
		public void ungrabFocus() {
		}
	}

    private class StageImpl implements Stage{

		@Override
		public ReadOnlyBooleanProperty getFocusProperty() {
			throw new IllegalStateException("Not implemented yet!");
		}

		@Override
		public boolean hasFocus() {
			return BaseContext.this.hasFocus();
		}

		@Override
        public void setScene(Scene scene) {
            BaseContext.this.setScene(scene);
        }

        @Override
        public FxContext getContext() {
            return BaseContext.this;
        }

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public ReadOnlyIntegerProperty getWidthProperty() {
            throw new IllegalStateException("Not implemented yet!");
		}

		@Override
		public ReadOnlyIntegerProperty getHeightProperty() {
            throw new IllegalStateException("Not implemented yet!");
		}
	}


//	private final BitSet keyStateSet = new BitSet(0xFF);

//	int retrieveKeyState() {
//		int embedModifiers = 0;
//
//		if (this.keyStateSet.get(KeyEvent.VK_SHIFT)) {
//			embedModifiers |= AbstractEvents.MODIFIER_SHIFT;
//		}
//
//		if (this.keyStateSet.get(KeyEvent.VK_CONTROL)) {
//			embedModifiers |= AbstractEvents.MODIFIER_CONTROL;
//		}
//
//		if (this.keyStateSet.get(KeyEvent.VK_ALT)) {
//			embedModifiers |= AbstractEvents.MODIFIER_ALT;
//		}
//
//		if (this.keyStateSet.get(KeyEvent.VK_META)) {
//			embedModifiers |= AbstractEvents.MODIFIER_META;
//		}
//		return embedModifiers;
//	}

//	Map<FxWindow, PopupSnapper>			snappers			= new IdentityHashMap<>();
//	List<PopupSnapper>					activeSnappers		= new CopyOnWriteArrayList<>();
//	static Map<FxWindow, BaseContext>	sceneContainerMap	= new ConcurrentHashMap<>();

//	static boolean						sceneAccessorHackInstalled;

//	static void installSceneAccessorHack() {
//
//		if (BaseContext.sceneAccessorHackInstalled) {
//			return;
//		}
//
//		try {
//			final Field f = SceneHelper.class.getDeclaredField("sceneAccessor");
//			f.setAccessible(true);
//			final SceneAccessor orig = (SceneAccessor) f.get(null);
//
//			final SceneAccessor sa = new SceneAccessor() {
//
//				@Override
//				public void setPaused(final boolean paused) {
//					orig.setPaused(paused);
//				}
//
//				@Override
//				public void parentEffectiveOrientationInvalidated(final Scene scene) {
//					orig.parentEffectiveOrientationInvalidated(scene);
//				}
//
//				@Override
//				public Accessible getAccessible(final Scene scene) {
//					return null;
//				}
//
//				@Override
//				public Camera getEffectiveCamera(final Scene scene) {
//					return orig.getEffectiveCamera(scene);
//				}
//
//				@Override
//				public Scene createPopupScene(final Parent root) {
//					final Scene scene = orig.createPopupScene(root);
//
//					scene.windowProperty().addListener(new ChangeListener<FxWindow>() {
//
//						@Override
//						public void changed(final javafx.beans.value.ObservableValue<? extends FxWindow> observable, final FxWindow oldValue, final FxWindow window) {
//							window.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
//
//								@Override
//								public void handle(final WindowEvent event) {
//									final BaseContext container = BaseContext.sceneContainerMap.get(((FxWindow) window).getOwnerWindow());
//									if (container != null) {
//										final PopupSnapper ps = new PopupSnapper(container, window, scene);
//										synchronized (container.snappers) {
//											container.snappers.put(window, ps);
//										}
//										ps.start();
//									}
//
//								}
//							});
//						};
//					});
//
//					scene.windowProperty().addListener(new ChangeListener<FxWindow>() {
//
//						@Override
//						public void changed(final javafx.beans.value.ObservableValue<? extends FxWindow> observable, final FxWindow oldValue, final FxWindow window) {
//							window.addEventHandler(WindowEvent.WINDOW_HIDDEN, new EventHandler<WindowEvent>() {
//
//								@Override
//								public void handle(final WindowEvent event) {
//									final BaseContext container = BaseContext.sceneContainerMap.get(((FxWindow) window).getOwnerWindow());
//									if (container != null) {
//
//										final PopupSnapper ps;
//										synchronized (container.snappers) {
//											ps = container.snappers.remove(window);
//										}
//										if (ps == null) {
//											System.out.println("Cannot find snapper for window " + window);
//										} else {
//											ps.stop();
//										}
//									}
//								}
//							});
//						};
//					});
//
//					return scene;
//				}
//
//				@Override
//				public void setTransientFocusContainer(final Scene scene, final javafx.scene.Node node) {
//
//				}
//			};
//
//			f.set(null, sa);
//		} catch (final Exception exc) {
//			exc.printStackTrace();
//		}
//
//		BaseContext.sceneAccessorHackInstalled = true;
//	}

//	/**
//	 * call via gui manager!
//	 *
//	 * @param rawInputListenerAdapter
//	 */
//	public void setEverListeningRawInputListener(final RawInputListener rawInputListenerAdapter) {
//		this.inputAdapter.setEverListeningRawInputListener(rawInputListenerAdapter);
//	}
//
//	public Parent getRootNode() {
//		return this.rootNode;
//	}


	@Override
	protected void finalize() throws Throwable {
		destroy();
		super.finalize();
	}

//	public ObservableList<Node> getRootChildren() {
//		if (this.rootNode instanceof Group) {
//			return ((Group) this.rootNode).getChildren();
//		} else if (this.rootNode instanceof Pane) {
//			return ((Pane) this.rootNode).getChildren();
//		} else {
//			return FXCollections.emptyObservableList();
//		}
//	}
//
}