package com.jme3.jfx.base;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

import java.util.function.Consumer;

/**
 * Created by jan on 06.06.16.
 */
public final class RenderSystem {

    private RenderSystem(){}

    /**
     * Creates a post ViewPort with the given size to whom the layers are rendered.
     *
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    public static RenderSystem renderToFullscreen(int screenWidth, int screenHeight){
        RenderSystem rs = new RenderSystem();
        rs.width = screenWidth;
        rs.height = screenHeight;
        rs.toTexture = false;
        return rs;
    }

    /**
     * Attaches a new scene to this viewPort, which contains the layers.
     *
     * @param viewPort
     * @return
     */
    public static RenderSystem renderToViewPort(ViewPort viewPort){
        RenderSystem rs = new RenderSystem();
        rs.viewPort = viewPort;
        rs.toTexture = false;
        return rs;
    }

    /**
     * Creates a pre ViewPort with the given size to whom the layers are rendered.
     * The ViewPort is rendered to a texture, which can be accessed after creation
     * with the accessor.
     *
     * @param width
     * @param height
     * @param accessor
     * @return
     */
    public static RenderSystem renderToTexture(int width, int height, Consumer<Texture2D> accessor){
        RenderSystem rs = new RenderSystem();
        rs.width = width;
        rs.height = height;
        rs.toTexture = true;
        rs.accessor = accessor;
        return rs;
    }

    /**
     * Creates a texture to whom the layers are rendered. This textur will be applied
     * to the geometry.
     *
     * @param width
     * @param height
     * @param geom
     * @return
     */
    public static RenderSystem renderToGeometry(int width, int height, Geometry geom){
        RenderSystem rs = new RenderSystem();
        rs.width = width;
        rs.height = height;
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
    private int width = 0;
    private int height = 0;
    private boolean toTexture = false;
    private Consumer<Texture2D> accessor;
    private Geometry targetGeometry = null;

    private boolean isPreView = false;
    private boolean isPostView = false;
    private FrameBuffer frameBuffer = null;

    void create(Context context){
        if(isCreated){
            throw new IllegalStateException("RenderSystem is already isCreated");
        }
        this.context = context;
        scene = new Node("Scene of "+context.getName());
        if(viewPort == null){
            Camera camera = new Camera(width, height);

            //START to texture rendering
            if(toTexture) {
                viewPort = context.getJFxManager().getApplication().getRenderManager().createPreView(
                        context.getName() + " view", camera);
                isPreView = true;
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

                // create offscreen framebuffer
                frameBuffer = new FrameBuffer(width, height, 1);

                //setup framebuffer's texture
                texture = new Texture2D(width, height, Image.Format.RGBA8);
                texture.setMinFilter(Texture.MinFilter.Trilinear);
                texture.setMagFilter(Texture.MagFilter.Bilinear);

                //setup framebuffer to use texture
                frameBuffer.setDepthBuffer(Image.Format.Depth);
                frameBuffer.setColorTexture(texture);

                //set viewport to render to offscreen framebuffer
                viewPort.setOutputFrameBuffer(frameBuffer);

                //inform accessor
                if(accessor != null) {
                    accessor.accept(texture);
                }

                if(targetGeometry != null){
                    Material material = new Material(context.getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                    material.setTexture("ColorMap", texture);
                    material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                    targetGeometry.setQueueBucket(RenderQueue.Bucket.Gui);
                    targetGeometry.setMaterial(material);
                }
            }
            //END to texture rendering
        }
        //only update scene state manually if we attach a new scene to the viewport
        context.getJFxManager().addOnUpdate(sceneStateUpdater);
        viewPort.attachScene(scene);
        isCreated = true;
    }

    private void updateSceneState(float tpf){
        scene.updateLogicalState(tpf);
        scene.updateGeometricState();
    }

    Node getScene(){
        return scene;
    }

    Texture2D getTexture(){
        return texture;
    }

    public int getWidth(){
        return viewPort.getCamera().getWidth();
    }

    public int getHeight(){
        return viewPort.getCamera().getHeight();
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
