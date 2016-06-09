package com.jme3.jfx;

import java.awt.*;
import java.util.function.BiFunction;

/**
 *
 * You can configure the Contexts provided by this library through this class.
 * It is not defined what happens when you change a configuration after launching a context.
 * This could happen:
 * nothing,
 * you need to restart the context to apply these changes,
 * the changes are instantly applied
 *
 */
public final class Configuration {

    boolean forceSingleLayer = false;
//    boolean staticLayers = true;
    String name = "";
//    InputAdapter inputAdapter = null;
    RenderSystem renderSystem = null;
    BiFunction<Context, Point, Point> mouseInputConverter = MouseInputConverters.Discard;


    /**
     * Set the RenderSystem
     * @param renderSystem
     * @return
     */
    public Configuration setRenderSystem(RenderSystem renderSystem){
        this.renderSystem = renderSystem;
        return this;
    }


    /**
     * The converter is always called from the jME3 Render Thread
     *
     * @param inputConverter default: MouseInputConverters.Discard
     * @return
     */
    public Configuration setMouseInputConverter(BiFunction<Context, Point, Point> inputConverter){
        this.mouseInputConverter = inputConverter;
        return this;
    }

//    /**
//     * Enable if the layers should always have the size of the context
//     * and shouldn't be moved
//     *
//     * @param staticLayers default: true
//     * @return the configuration object
//     */
//    public Configuration setStaticLayers(boolean staticLayers){
//        this.staticLayers = staticLayers;
//        return this;
//    }

    /**
     * Call this if you want a context with only one layer
     * @param singleLayer default: false
     * @return the configuration object
     */
    public Configuration setForceSingleLayer(boolean singleLayer){
        this.forceSingleLayer = singleLayer;
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

//    /**
//     * If inputAdapter is null the InputAdapter of the JFxManager instance is used.
//     * If you don't want the context to listen to any input, create
//     * a new InputAdapter and don't call its methods.
//     *
//     * @param inputAdapter default: null
//     * @return the configuration object
//     */
//    public Configuration setInputAdapter(InputAdapter inputAdapter){
//        this.inputAdapter = inputAdapter;
//        return this;
//    }
}
