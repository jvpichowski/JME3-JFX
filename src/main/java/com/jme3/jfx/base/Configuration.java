package com.jme3.jfx.base;

import com.jme3.jfx.InputAdapter;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.Texture2D;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

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
    int width = 0;
    int height = 0;



    RenderSystem renderSystem;
    BiFunction<Context, Point, Point> inputConverter;

    public Configuration setRenderSystem(RenderSystem renderSystem){
        this.renderSystem = renderSystem;
        return this;
    }


    public Configuration setInputConverter(BiFunction<Context, Point, Point> inputConverter){
        this.inputConverter = inputConverter;
        return this;
    }

    public Configuration setTextureAccessor(Consumer<Texture2D> accessor){

        return this;
    }


    /**
     * Set the width of the context. Layers could be smaller
     * if context has not static layers
     * @param width default: 0
     * @return the configuration object
     */
    public Configuration setWidth(int width){
        this.width = width;
        return this;
    }

    /**
     * Set the height of the context. Layers could be smaller
     * if context has not static layers
     * @param height default: 0
     * @return the configuration object
     */
    public Configuration setHeight(int height){
        this.height = height;
        return this;
    }


    /**
     * If a view port is set the context is drawn in full size to that view port.
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
