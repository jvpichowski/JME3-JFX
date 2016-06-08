package com.jme3.jfx.base;

import com.jme3.app.Application;
import com.jme3.jfx.FxApplication;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Layer;

import java.util.List;

/**
 * Public api
 *
 * The Context defines a frame. This frame could be rendered to
 * geometries or to viewports. The Context holds instances of Java
 * Fx Applications encapsulated into Layers. The Context handles
 * input and the drawing of the Layers.
 *
 * Created by jan on 01.06.16.
 */
public interface Context {

    /**
     * Default Implementations
     *
     * @param config
     * @return
     */
    static Context create(Configuration config){
        if(config.singleLayer){
            return new SingleLayerContext(config.renderSystem, config.inputConverter);
        }else{
            Context result = new MultiLayerContext(config.renderSystem, config.inputConverter, config.staticLayers);
            result.setName(config.name);
            return result;
        }
    }

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

    /**
     *
     * @return the image object in which the FxApplication is painted
     */
//    Image getImage();

    /**
     * After its creation you could add layers to the context
     *
     * @return true if the context is created otherwise false
     */
    boolean isCreated();

    /**
     * FOR INTERNAL USE ONLY - will throw exception if used twice
     *
     * is called at jfxManager.createLayer
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
