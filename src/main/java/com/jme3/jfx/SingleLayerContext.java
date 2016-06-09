package com.jme3.jfx;

import com.sun.javafx.embed.AbstractEvents;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Implementation
 *
 * Default context for fullscreen application.
 *
 * Created by jan on 16.05.16.
 */
final class SingleLayerContext extends BaseContext{

    private final RenderSystem renderSystem;
    private final BiFunction<Context, Point, Point> inputConverter;

    private LayerImpl layer;



    SingleLayerContext(RenderSystem renderSystem, BiFunction<Context, Point, Point> inputConverter){
        this.renderSystem = renderSystem;
        this.inputConverter = inputConverter;
    }

    @Override
    public Layer createLayer(FxApplication application){
        if(layer != null){
            throw new IllegalStateException("This context doesn't allow multiple layers");
        }
        FxContainer fxContainer = new FxContainer(renderSystem.getWidth(), renderSystem.getHeight());
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
        renderSystem.destroy();
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
        return Arrays.asList(layer);
    }

    @Override
    protected void reorderLayers() {
        //nothing to do here because there is only one layer
    }

    @Override
    public void pushFront(Layer layer){
        //nothing to do here because there is only one layer
    }

    @Override
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

    //********************************************************************//
    // Some getters                                                       //
    //********************************************************************//


    @Override
    public int getHeight() {
        return layer.getHeight();
    }

    @Override
    public int getWidth() {
        return layer.getWidth();
    }

    private final class LayerImpl extends BaseLayer implements InputAdapter.InputListener {

        public LayerImpl(FxContainer fxContainer){
            super(SingleLayerContext.this, fxContainer);
        }

        @Override
        public void show() {
            getApplication().enqueue(() -> {
                renderSystem.create(SingleLayerContext.this, getFxContainer().getTexture());
            });
        }

        @Override
        public void close() {
            getApplication().enqueue(renderSystem::destroy);
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

            //converting happens in the context
            Point contextClick = inputConverter.apply(SingleLayerContext.this, new Point(jME_x, jME_y));
            if(contextClick == null){
                return false;
            }
            final int x = contextClick.x;
            final int y = contextClick.y;

            //if not covered return false here - release focus?

            if(eventType == AbstractEvents.MOUSEEVENT_PRESSED){
                grabFocus();
            }
            if(eventType == AbstractEvents.MOUSEEVENT_RELEASED){
                grabFocus();
            }
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
