package com.jme3.jfx.base;

import com.jme3.app.Application;
import com.jme3.jfx.FxApplication;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Layer;
import com.jme3.jfx.fxcontext.input.InputAdapter;

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
        if(config.viewPort != null) {
            return new FullScreen(config.viewPort);
        }
        return null;
    }

    Layer createLayer(FxApplication application);
    List<Layer> getLayers();

    /**
     * Really needed?
     * @return
     */
//    EmbeddedSceneInterface getSceneInterface();

    void setName(String name);

    String getName();

    /**
     *
     * @return the image object in which the FxApplication is painted
     */
//    Image getImage();

    boolean isCreated();

    /**
     * FOR INTERNAL USE ONLY
     *
     * is called at jfxManager.createLayer
     * @param jfxManager
     */
    void create(JFxManager jfxManager);

    void restart();

    /**
     * Destroy this fxcontext
     */
    void destroy();


    /**
     *
     * @return the InputAdapter
     */
    InputAdapter getInputAdapter();

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
     * grab the focus to this scene
     */
    void grabFocus();

    /**
     * remove the focus from this scene
     */
    void loseFocus();

    /**
     *
     * @return true if the focus is grabbed otherwise false
     */
    boolean hasFocus();
}
