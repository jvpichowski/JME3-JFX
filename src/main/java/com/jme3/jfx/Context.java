package com.jme3.jfx;

import com.jme3.app.Application;

import java.util.List;

/**
 * The Context defines a frame. This frame could be rendered to
 * geometries or to viewports. The Context holds instances of Java
 * Fx Applications encapsulated into Layers. The Context handles
 * input and the drawing of the Layers.
 *
 */
public interface Context {

    /**
     * Create a default implementation.
     *
     * @param config
     * @return
     */
    static Context create(Configuration config){
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }
        if (config.renderSystem == null) {
            throw new IllegalArgumentException("Config must contain a RenderSystem");
        }
        if(config.mouseInputConverter == null){
            throw new IllegalArgumentException("Config must contain a MouseInputConverter");
        }
        if(config.name == null){
            throw new IllegalArgumentException("Config must contain a Name");
        }
        Context result;
        if(config.forceSingleLayer){
            result = new SingleLayerContext(config.renderSystem, config.mouseInputConverter);
        }else{
            result = new MultiLayerContext(config.renderSystem, config.mouseInputConverter, true);
        }
        result.setName(config.name);
        return result;
    }

    /**
     * Create a new Layer.
     *
     * @param application
     * @return
     */
    Layer createLayer(FxApplication application);

    /**
     * ordered from front to back
     * @return
     */
    List<Layer> getLayers();


    void setName(String name);

    String getName();

    int getHeight();

    int getWidth();

//    /**
//     *
//     * @return the image object in which the FxApplication is painted
//     */
//    Image getImage();

    /**
     * After its creation you could add layers to the context
     *
     * @return true if the context is created otherwise false
     */
    boolean isCreated();

    /**
     * Should be called by JFxManager.
     *
     * @param jfxManager
     */
    void create(JFxManager jfxManager);

    void restart();

    /**
     * Destroy this context
     */
    void destroy();

    /**
     *
     * @return the jME3 Application
     */
    Application getApplication();

    /**
     *
     * @return the manger which created this fxcontext
     */
    JFxManager getJFxManager();

    /**
     * manually grab the focus to this context
     */
    void grabFocus();

    /**
     * manually remove the focus from this context
     */
    void loseFocus();

    /**
     *
     * @return true if the focus is grabbed otherwise false
     */
    boolean hasFocus();
}
