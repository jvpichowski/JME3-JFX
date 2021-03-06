package com.jme3.jfx;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

import java.util.function.Consumer;

/**
 * Handles how the layers are rendered. They could be rendered to
 * a ViewPort, to a Texture2D, to the Fullscreen in front of the
 * gui viewport, or to a Geometry. To implement special behavior
 * use the first two. The latter ones are also based on them.
 *
 */
public final class RenderSystem {


    private final int width;
    private final int height;

    private RenderSystem(int width, int height){
        this.width = width;
        this.height = height;
    }

    //useful because of reshape
//    public static RenderSystem renderToProcessor(Consumer<SceneProcessor> processorConsumer){
//
//    }
//
//    public static RenderSystem renderToFilter(Consumer<Filter> filterConsumer){
//
//    }

    /**
     * Creates a post ViewPort with the given size to which the layers are rendered.
     *
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    public static RenderSystem renderToFullscreen(int screenWidth, int screenHeight){
        RenderSystem rs = new RenderSystem(screenWidth, screenHeight);
        rs.toTexture = false;
        return rs;
    }

    /**
     * Attaches a new scene which contains the layers to this viewPort.
     *
     * @param viewPort
     * @return
     */
    public static RenderSystem renderToViewPort(ViewPort viewPort){
        RenderSystem rs = new RenderSystem(viewPort.getCamera().getWidth(), viewPort.getCamera().getHeight());
        return rs;
    }

    /**
     * Creates a pre ViewPort with the given size to which the layers are rendered.
     * The ViewPort is rendered to a texture that can be accessed after creation
     * with the accessor.
     *
     * @param width
     * @param height
     * @param accessor
     * @return
     */
    public static RenderSystem renderToTexture(int width, int height, Consumer<Texture2D> accessor){
        RenderSystem rs = new RenderSystem(width, height);
        rs.toTexture = true;
        rs.accessor = accessor;
        return rs;
    }

    //TODO fix FrameBuffer usage, There is a transparency bug.
    /**
     * Creates a texture to which the layers are rendered. This texture will be applied
     * to the geometry.
     *
     * @param width
     * @param height
     * @param geom
     * @return
     */
    public static RenderSystem renderToGeometry(int width, int height, Geometry geom){
        RenderSystem rs = new RenderSystem(width, height);
        rs.toTexture = true;
        rs.targetGeometry = geom;
        return rs;
    }

    private final Consumer<Float> sceneStateUpdater = tpf -> updateSceneState(tpf);
    private Node scene;
    private boolean isCreated = false;
    private Context context;
    private Texture2D texture = null;

    //Settings
    private ViewPort viewPort = null;
    private boolean toTexture = false;
    private Consumer<Texture2D> accessor;
    private Geometry targetGeometry = null;

    private boolean isPreView = false;
    private boolean isPostView = false;
    private FrameBuffer frameBuffer = null;
    private boolean singleLayer = false;

    /**
     *
     * @param context
     * @param contextTexture if the context is in single layer mode
     *                       use the texture of the layer otherwise
     *                       null
     */
    void create(Context context, Texture2D contextTexture){
        if(isCreated){
            throw new IllegalStateException("RenderSystem is already isCreated");
        }
        if(contextTexture != null){
            singleLayer = true;
        }

        this.context = context;
        scene = new Node("Scene of "+context.getName());
        if(viewPort == null){
            Camera camera = new Camera(width, height);

            //START to texture rendering
            if(toTexture) {
                if(contextTexture == null) {
                    viewPort = context.getJFxManager().getApplication().getRenderManager().createPreView(
                            context.getName() + " view", camera);
                    isPreView = true;
                }
            }
            //END to texture rendering
            else{
                viewPort = context.getJFxManager().getApplication().getRenderManager().createPostView(
                        context.getName()+" view", camera);
                isPostView = true;
            }
            viewPort.setClearFlags(false, false, false);
            viewPort.setBackgroundColor(new ColorRGBA(0,0,0,0));

            //START to texture rendering
            if(toTexture) {

                if(contextTexture != null){
                    // When the context is in single layer mode it will
                    // pass the texture of the layer through. There is no
                    // need to create an offscreen renderer to get the
                    // texture because we have it already
                    texture = contextTexture;
                }else {
                    // create offscreen framebuffer
                    frameBuffer = new FrameBuffer(width, height, 1);

                    //setup framebuffer's texture
                    texture = new Texture2D(width, height, Image.Format.RGBA8);
                    texture.setMinFilter(Texture.MinFilter.Trilinear);
                    texture.setMagFilter(Texture.MagFilter.Bilinear);

                    //setup framebuffer to use texture
                    frameBuffer.setDepthBuffer(Image.Format.Depth);
                    frameBuffer.setColorTexture(texture);
                    //TODO frameBuffer.setUpdateNeeded(); on transparency rewrite?

                    //set viewport to render to offscreen framebuffer
                    viewPort.setOutputFrameBuffer(frameBuffer);
                }

                //inform accessor
                if(accessor != null) {
                    accessor.accept(texture);
                }

                if(targetGeometry != null){
                    Material material = new Material(context.getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                    material.setTexture("ColorMap", texture);
                    material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
//                    targetGeometry.setQueueBucket(RenderQueue.Bucket.Gui);
                    targetGeometry.setQueueBucket(RenderQueue.Bucket.Transparent);
                    targetGeometry.setMaterial(material);
                }
            }
            //END to texture rendering
        }
        //only update scene state manually if we attach a new scene to the viewport
        context.getJFxManager().addOnUpdate(sceneStateUpdater);
        if(viewPort != null) {// it is null in single layer mode and render toTexture
            viewPort.attachScene(scene);
            if(contextTexture != null){
                Geometry geom = new Geometry(context.getName()+" geometry", new Quad(getWidth(), getHeight(), true));

                Material material = new Material(context.getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                material.setTexture("ColorMap", contextTexture);
                material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                geom.setQueueBucket(RenderQueue.Bucket.Gui);//Change to Transparent?
                geom.setMaterial(material);
                geom.setLocalTranslation(0,0,1);
                scene.attachChild(geom);
            }
        }
        isCreated = true;
    }

    private void updateSceneState(float tpf){
        scene.updateLogicalState(tpf);
        scene.updateGeometricState();
    }

    Node getScene(){
        if(singleLayer){
            throw new IllegalStateException("You can't access the scene in single layer mode");
        }
        return scene;
    }

//    Texture2D getTexture(){
//        return texture;
//    }

    /**
     * Will be available before creation
     * @return
     */
    public int getWidth(){
        return width;
    }

    /**
     * Will be available before creation
     * @return
     */
    public int getHeight(){
        return height;
    }

    void destroy(){
        isCreated = false;
        context.getJFxManager().removeOnUpdate(sceneStateUpdater);
        viewPort.detachScene(scene);
        if(isPreView){
            context.getJFxManager().getApplication().getRenderManager().removePreView(viewPort);
        }
        if(isPostView){
            context.getJFxManager().getApplication().getRenderManager().removePostView(viewPort);
        }
        if(frameBuffer != null){
            frameBuffer.dispose();
        }
    }

}
