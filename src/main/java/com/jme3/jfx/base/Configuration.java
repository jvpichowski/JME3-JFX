package com.jme3.jfx.base;

import com.jme3.renderer.ViewPort;

/**
 * public api
 *
 * You can configure the Contexts provided by this library through this class.
 * It is not defined what happens when you change a configuration after launching a context.
 * This could happen:
 * - nothing
 * - you need to restart the context to apply changes
 * - the changes are instantly applied
 *
 * Created by jan on 01.06.16.
 */
public final class Configuration {

    boolean transparent;
    ViewPort viewPort;

    public Configuration setViewPort(ViewPort viewPort){
        this.viewPort = viewPort;
        return this;
    }
}
