package com.jme3.jfx.base;

import com.jme3.jfx.FxApplication;
import com.jme3.jfx.InputAdapter;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Layer;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by jan on 06.06.16.
 */
final class MultiLayerFullscreen extends BaseContext {

    private InputAdapter.InputListener inputListener;

    private ViewPort layerViewPort;
    private Node layerNode;
    private final Consumer<Float> updater = tpf -> onUpdate(tpf);

    private boolean hasFocus = false;

    private final ViewPort viewPort;
    private final int width;
    private final int height;
    private final boolean staticLayers;

    public MultiLayerFullscreen(ViewPort viewPort, boolean staticLayers) {
        this.viewPort = viewPort;
        this.width = viewPort.getCamera().getWidth();
        this.height = viewPort.getCamera().getHeight();
        this.staticLayers = staticLayers;
    }

    /**
     * FOR INTERNAL USE ONLY
     * <p>
     * is called at jfxManager.createLayer
     *
     * @param jfxManager
     */
    @Override
    public void create(JFxManager jfxManager) {
        if(isCreated()){
            throw new IllegalStateException("Context is already created");
        }
        super.create(jfxManager);
        Camera layerCam = new Camera(width, height);
        layerViewPort = getJFxManager().getApplication().getRenderManager().createPostView(getName(), layerCam);
        layerViewPort.setClearFlags(false, false, false);
        layerNode = new Node("Root node of "+getName());
        layerViewPort.attachScene(layerNode);
        inputListener = new InputListenerImpl();
        getJFxManager().getInputAdapter().register(inputListener);
        getJFxManager().addOnUpdate(updater);
    }

    private void onUpdate(float tpf){
        layerNode.updateLogicalState(tpf);
//        layerNode.updateModelBound();
        layerNode.updateGeometricState();
    }


    /**
     * Destroy this context
     */
    @Override
    public void destroy() {
        if(!isCreated()){
            super.destroy();
            return;
        }
        getJFxManager().removeOnUpdate(updater);
        getJFxManager().getInputAdapter().unregister(inputListener);
        inputListener = null;
        layerViewPort.detachScene(layerNode);
        getJFxManager().getApplication().getRenderManager().removePostView(layerViewPort);
        super.destroy();
    }

    @Override
    protected void reorderLayers() {
        //TODO maybe slow -> use concurrent data structure and enqueue all together
        //and use for loop to get index
        getLayers().forEach(l -> ((LayerImpl)l).setZPosition(getLayers().indexOf(l)));
    }

    @Override
    public Layer createLayer(FxApplication application) {
        FxContainer fxContainer = new FxContainer(width, height);
        fxContainer.create(this);
        LayerImpl layer = new LayerImpl(fxContainer);
        try {
            application.start(layer);
            addLayer(layer);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return layer;
    }



    /**
     * manually grab the focus to this context
     */
    @Override
    public void grabFocus() {
        hasFocus = true;
    }

    /**
     * manually remove the focus from this context
     */
    @Override
    public void loseFocus() {
        hasFocus = false;
    }

    /**
     * @return true if the focus is grabbed otherwise false
     */
    @Override
    public boolean hasFocus() {
        return hasFocus;
    }


    private final class InputListenerImpl implements InputAdapter.InputListener {

        /**
         * @param eventType     AbstractEvents.MOUSEEVENT_MOVED |
         *                      AbstractEvents.MOUSEEVENT_WHEEL|
         *                      AbstractEvents.MOUSEEVENT_PRESSED |
         *                      AbstractEvents.MOUSEEVENT_RELEASED
         * @param button        AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON |
         *                      AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON |
         *                      AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON |
         *                      AbstractEvents.MOUSEEVENT_NONE_BUTTON
         * @param wheelRotation
         * @param jME_x         screen x
         * @param jME_y         screen y
         * @return true if consumed by context
         */
        @Override
        public boolean applyMouseInput(int eventType, int button, int wheelRotation, int jME_x, int jME_y) {

            //converting happens in the context
            final int x = jME_x;
            final int y = height - jME_y;

            //ordered because getLayers() returns an ordered list
            Optional<Layer> consumer = getLayers().stream().filter(layer -> {
                LayerImpl l = (LayerImpl) layer;
                //TODO move to base class
                if(x < l.getX() || x > l.getX()+l.getWidth()){
                    return false;
                }
                if(y < l.getY() || y > l.getY()+l.getHeight()){
                    return false;
                }
                if(l.applyMouseInput(getJFxManager(), eventType, button, wheelRotation,
                        x-(int)l.getX(), y-(int)l.getY(), x, y)){
                    return true;
                }
                return false;
            }).findFirst(); //stops searching when one layer returns true

            if(consumer.isPresent()){
                grabFocus();
            }else{
                loseFocus();
            }

            return consumer.isPresent();
        }

        /**
         * @param eventType  AbstractEvents.KEYEVENT_PRESSED |
         *                   AbstractEvents.KEYEVENT_TYPED |
         *                   AbstractEvents.KEYEVENT_RELEASED
         * @param fx_keycode
         * @return true if consumed by context
         */
        @Override
        public boolean applyKeyInput(int eventType, int fx_keycode) {
            if(!hasFocus()){
                return false;
            }
            getLayers().forEach(layer -> ((LayerImpl)layer).applyKeyInput(getJFxManager(), eventType, fx_keycode));
            return true;
        }
    }


    private final class LayerImpl extends BaseLayer {

        private Geometry geom;
        private Material material;

        private float x = 0;
        private float y = 0;

        public LayerImpl(FxContainer fxContainer) {
            super(MultiLayerFullscreen.this, fxContainer);
        }

        public void setZPosition(float z){
            getApplication().enqueue(() -> geom.setLocalTranslation(
                    geom.getLocalTranslation().getX(),
                    geom.getLocalTranslation().getY(),
                    z));
        }

        public float getX(){
            return x;
        }

        public float getY(){
            return y;
        }

        @Override
        public void setPosition(float x, float y) {
            if(staticLayers){
                throw new IllegalStateException("Not supported by this context");
            }
            this.x = x;
            this.y = y;
        }

        @Override
        public void setSize(float width, float height) {
            if(staticLayers) {
                throw new IllegalStateException("Not supported by this context");
            }
        }

        @Override
        public void show() {
            getApplication().enqueue(() -> {
                geom = new Geometry(getName(), new Quad(getWidth(), getHeight(), true));

                material = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                material.setTexture("ColorMap", getFxContainer().getTexture());
                material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                geom.setQueueBucket(RenderQueue.Bucket.Gui);
                geom.setMaterial(material);
                ((Node) viewPort.getScenes().get(0)).attachChild(geom);
                //layerNode.attachChild(geom);
            });
        }

        @Override
        public void close() {
            getApplication().enqueue(() ->  geom.removeFromParent());
            removeLayer(this);
            super.close();
        }
    }
}
