package com.jme3.jfx.base;

import com.jme3.jfx.FxApplication;
import com.jme3.jfx.InputAdapter;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Layer;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.sun.javafx.embed.AbstractEvents;
import javafx.scene.Scene;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation
 *
 * Default context for fullscreen application.
 *
 * Created by jan on 16.05.16.
 */
final class FullScreen extends BaseContext{

    private ViewPort viewPort;
    private Geometry geom;
    private Material material;

    private LayerImpl layer;

    FullScreen(ViewPort viewPort){
        this.viewPort = viewPort;
    }

    @Override
    public Layer createLayer(FxApplication application){
        if(layer != null){
            throw new IllegalStateException("This fxcontext doesn't allow multiple layers");
        }
        FxContainer fxContainer = new FxContainer(viewPort.getCamera().getWidth(), viewPort.getCamera().getHeight());
        fxContainer.create(this);
        layer = new LayerImpl(fxContainer);
        try {
            application.start(layer);
            //no need for complex input handling because we only have one layer
            //let this layer do the work and don't spam the context
            getJFxManager().getInputAdapter().register(layer);
        } catch (Exception e) {
            layer = null;
            e.printStackTrace();
            return null;
        }
        return layer;
    }

    @Override
    public List<Layer> getLayers() {
        List<Layer> result = new ArrayList<>();
        result.add(layer);
        return result;
    }

    @Override
    public void create(JFxManager jFxManager){
        super.create(jFxManager);
    }



    @Override
    public void destroy() {
        geom.removeFromParent();
        if(layer != null){
            getJFxManager().getInputAdapter().unregister(layer);
            layer.close();
            layer = null;
        }
        super.destroy();
    }


    //******************************************************************//
    // Input management                                                 //
    //******************************************************************//

    /**
     * grab the focus to this scene
     */
    @Override
    public void grabFocus() {
        if(layer != null){
            layer.grabFocus();
        }
    }

    /**
     * remove the focus from this scene
     */
    @Override
    public void loseFocus() {
        if(layer != null){
            layer.loseFocus();
        }
    }

    /**
     * @return true if the focus is grabbed otherwise false
     */
    @Override
    public boolean hasFocus() {
        if(layer != null){
            return layer.hasFocus();
        }
        return false;
    }


    private final class LayerImpl implements Layer, InputAdapter.InputListener {

        private final FxContainer fxContainer;
        private InputMode inputMode = InputMode.LEAK;
        private boolean hasFocus = false;

        public LayerImpl(FxContainer fxContainer){
            this.fxContainer = fxContainer;
        }

        @Override
        public void setScene(Scene scene){
            fxContainer.setScene(scene);
        }

        @Override
        public void loseFocus() {
            hasFocus = false;
        }

        @Override
        public void grabFocus() {
            hasFocus = true;
        }

        @Override
        public boolean hasFocus() {
            return hasFocus;
        }

        @Override
        public void setInputMode(InputMode mode) {
            this.inputMode = mode;
        }

        @Override
        public void show() {
            getApplication().enqueue(() -> {
                geom = new Geometry(getName(), new Quad(fxContainer.getWidth(), fxContainer.getHeight(), true));

                material = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                material.setTexture("ColorMap", fxContainer.getTexture());
                material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                geom.setQueueBucket(RenderQueue.Bucket.Gui);
                geom.setMaterial(material);
                ((Node) viewPort.getScenes().get(0)).attachChild(geom);
            });
        }

        @Override
        public void close() {
            geom.removeFromParent();
            fxContainer.destroy();
            //to prevent endless loop
            //it is correct to first clear the layer
            //and afterwards destroy the context
            layer = null;
            destroy();
        }

        @Override
        public void setTitle(String title) {
            fxContainer.setName(title);
        }

        @Override
        public void toFront() {
            //nothing to do because there is only one layer
        }

        @Override
        public void toBack() {
            //nothing to do because there is only one layer
        }

        @Override
        public void setPosition(float x, float y) {
            throw new IllegalStateException("This layer can't be moved");
        }

        @Override
        public void setSize(float width, float height) {
            throw new IllegalStateException("This layer can't be resized");
        }

        @Override
        public int getWidth() {
            return fxContainer.getWidth();
        }

        @Override
        public int getHeight() {
            return fxContainer.getHeight();
        }


        @Override
        public boolean applyMouseInput(int eventType, int button, int wheelRotation, int jME_x, int jME_y){
            if(!fxContainer.isActive()){
                return false;
            }
            //if not covered return false here

            if(eventType == AbstractEvents.MOUSEEVENT_PRESSED){
                grabFocus();
            }
            if(eventType == AbstractEvents.MOUSEEVENT_RELEASED){
                grabFocus();
            }

            //converting happens in the context
            final int x = jME_x;
            final int y = Math.round(getHeight()) - jME_y;
            fxContainer.mouseEvent(eventType, button,
                    getJFxManager().getInputAdapter().getMouseButtonState(0),
                    getJFxManager().getInputAdapter().getMouseButtonState(1),
                    getJFxManager().getInputAdapter().getMouseButtonState(2),
                    x, y, x, y, //our layer has always the size of the context
                    getJFxManager().getInputAdapter().getKeyState(KeyEvent.VK_SHIFT),
                    getJFxManager().getInputAdapter().getKeyState(KeyEvent.VK_CONTROL),
                    getJFxManager().getInputAdapter().getKeyState(KeyEvent.VK_ALT),
                    getJFxManager().getInputAdapter().getKeyState(KeyEvent.VK_META), wheelRotation,
                    button == AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON);

            return true;
        }

        @Override
        public boolean applyKeyInput(int eventType, int fx_keycode) {
            if(!fxContainer.isActive()){
                return false;
            }
            if(!hasFocus()){
                return false;
            }
            fxContainer.keyEvent(eventType, fx_keycode,
                    new char[] { getJFxManager().getInputAdapter().getKeyChar(fx_keycode) },
                    getJFxManager().getInputAdapter().getEmbeddedModifiers());
            return true;
        }
    }
}
