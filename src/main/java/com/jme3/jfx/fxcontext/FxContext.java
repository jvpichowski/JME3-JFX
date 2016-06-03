package com.jme3.jfx.fxcontext;

import com.jme3.app.Application;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Stage;
import com.jme3.jfx.input.InputAdapter;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.texture.Image;
import com.sun.javafx.embed.EmbeddedSceneInterface;

/**
 * Created by jan on 28.04.16.
 * Definierte umgebung, die das starten von FXApps innerhlab dieser Umgebung ermöglicht.
 */
public interface FxContext {

    static FxContext createContext(InputAdapter inputAdapter, Geometry geom, int width, int height){
        return new GeometryContext(inputAdapter, geom, width, height);
    }

    static FxContext createContext(InputAdapter inputAdapter, ViewPort viewPort){
        return new FScreenContext(inputAdapter, viewPort);
    }

    /**
     * Really needed?
     * @return
     */
    EmbeddedSceneInterface getSceneInterface();

    void setName(String name);

    String getName();

    /**
     *
     * @return the image object in which the FxApplication is painted
     */
    Image getImage();

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
     * @return the primary stage
     */
    @Deprecated
    Stage getStage();

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
