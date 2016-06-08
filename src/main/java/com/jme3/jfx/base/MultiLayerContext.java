package com.jme3.jfx.base;

import com.jme3.jfx.FxApplication;
import com.jme3.jfx.InputAdapter;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Layer;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

import java.awt.*;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Created by jan on 06.06.16.
 */
class MultiLayerContext extends BaseContext {

    private InputAdapter.InputListener inputListener;

    private Node layerNode;

    private boolean hasFocus = false;

    private final boolean staticLayers;
    private final RenderSystem renderSystem;
    private final BiFunction<Context, Point, Point> inputConverter;

    public MultiLayerContext(RenderSystem renderSystem, BiFunction<Context, Point, Point> inputConverter, boolean staticLayers) {
        this.staticLayers = staticLayers;
        this.renderSystem = renderSystem;
        this.inputConverter = inputConverter;
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
        super.create(jfxManager);
        renderSystem.create(this, null);
        layerNode = renderSystem.getScene();
        inputListener = new InputListenerImpl();
        getJFxManager().getInputAdapter().register(inputListener);
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
        getJFxManager().getInputAdapter().unregister(inputListener);
        inputListener = null;
        renderSystem.destroy();
        super.destroy();
    }

    @Override
    protected void reorderLayers() {
        //TODO maybe slow -> use concurrent data structure and enqueue all together
        //and use for loop to get index
        getLayers().forEach(l -> ((LayerImpl)l).setOrderingPosition(getLayers().indexOf(l)));
    }

    @Override
    public Layer createLayer(FxApplication application) {
        FxContainer fxContainer = new FxContainer(renderSystem.getWidth(), renderSystem.getHeight());
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

    @Override
    public int getHeight() {
        return renderSystem.getHeight();
    }

    @Override
    public int getWidth() {
        return renderSystem.getWidth();
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

            Point click = inputConverter.apply(MultiLayerContext.this, new Point(jME_x, jME_y));
            if(click == null){
                return false;
            }
            int x = click.x;
            int y = click.y;

            //ordered because getLayers() returns an ordered list
            Optional<Layer> consumer = getLayers().stream().filter(layer -> {
                LayerImpl l = (LayerImpl) layer;
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

        public LayerImpl(FxContainer fxContainer) {
            super(MultiLayerContext.this, fxContainer);
        }

        public void setOrderingPosition(float z){
            getApplication().enqueue(() -> geom.setLocalTranslation(
                    geom.getLocalTranslation().getX(),
                    geom.getLocalTranslation().getY(),
                    z+1));//just in front of the camera
        }

        @Override
        public void setPosition(float x, float y) {
            if(staticLayers){
                throw new IllegalStateException("Not supported by this context");
            }
            setX(x);
            setY(y);
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
                layerNode.attachChild(geom);
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
