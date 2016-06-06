package com.jme3.jfx.base;

import com.jme3.jfx.InputAdapter;
import com.jme3.renderer.ViewPort;

/**
 * public api
 *
 * You can configure the Contexts provided by this library through this class.
 * It is not defined what happens when you change a configuration after launching a context.
 * This could happen:
 * - nothing
 * - you need to restart the context to applyMouseInput changes
 * - the changes are instantly applied
 *
 * Created by jan on 01.06.16.
 */
public final class Configuration {

    boolean transparent;
    ViewPort viewPort;
    boolean singleLayer = true;
    boolean staticLayers = true;
    String name = "";
    InputAdapter inputAdapter = null;

    /**
     *
     * @param viewPort default: null
     * @return the configuration object
     */
    public Configuration setViewPort(ViewPort viewPort){
        this.viewPort = viewPort;
        return this;
    }

    /**
     * Enable if the layers should always have the size of the context
     * and shouldn't be moved
     *
     * @param staticLayers default: true
     * @return the configuration object
     */
    public Configuration setStaticLayers(boolean staticLayers){
        this.staticLayers = staticLayers;
        return this;
    }

    /**
     * Call this if you want a context with only one layer
     * @param singleLayer default: true
     * @return the configuration object
     */
    public Configuration setSingleLayer(boolean singleLayer){
        this.singleLayer = singleLayer;
        return this;
    }

    /**
     * Sets the name of the context
     * @param name default: ""
     * @return the configuration object
     */
    public Configuration setName(String name){
        this.name = name;
        return this;
    }

    /**
     * If inputAdapter is null the InputAdapter of the JFxManager instance is used.
     * If you don't want the context to listen to any input, create
     * a new InputAdapter and don't call its methods.
     *
     * @param inputAdapter default: null
     * @return the configuration object
     */
    public Configuration setInputAdapter(InputAdapter inputAdapter){
        this.inputAdapter = inputAdapter;
        return this;
    }
}
