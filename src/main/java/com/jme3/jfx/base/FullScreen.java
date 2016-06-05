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
import java.util.Collections;
import java.util.LinkedList;
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

    //***************************************************************//
    // Layer Handling                                                //
    //***************************************************************//


    @Override
    public List<Layer> getLayers() {
        List<Layer> layers = new LinkedList<>();
        layers.add(layer);
        return layers;
    }

    public void pushFront(Layer layer){
        //nothing to do here because there is only one layer
    }

    public void pushBack(Layer layer){
        //nothing to do here because there is only one layer
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


    private final class LayerImpl extends BaseLayer implements InputAdapter.InputListener {

        public LayerImpl(FxContainer fxContainer){
            super(FullScreen.this, fxContainer);
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
            });
        }

        @Override
        public void close() {
            geom.removeFromParent();
            super.close();
            //to prevent endless loop
            //it is correct to first clear the layer
            //and afterwards destroy the context
            layer = null;
            destroy();
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
        public boolean applyMouseInput(int eventType, int button, int wheelRotation, int jME_x, int jME_y){
            if(!getFxContainer().isActive()){
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
            getFxContainer().mouseEvent(eventType, button,
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
            if(!getFxContainer().isActive()){
                return false;
            }
            if(!hasFocus()){
                return false;
            }
            getFxContainer().keyEvent(eventType, fx_keycode,
                    new char[] { getJFxManager().getInputAdapter().getKeyChar(fx_keycode) },
                    getJFxManager().getInputAdapter().getEmbeddedModifiers());
            return true;
        }
    }
}
