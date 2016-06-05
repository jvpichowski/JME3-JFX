package com.jme3.jfx.base;

import com.jme3.app.Application;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation
 *
 * Created by jan on 01.06.16.
 */
abstract class BaseContext implements Context {

    private Application application;
    private JFxManager jFxManager;
    private String name = "";
    private boolean created = false;

    //LinkedList is faster for rearranging elements (pushFront/Back)
    private List<Layer> layers = new LinkedList<>();

    /**
     * Really needed?
     *
     * @param name
     * @return
     */
    @Override
    public final void setName(String name) {
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public boolean isCreated() {
        return created;
    }

    /**
     * FOR INTERNAL USE ONLY
     * <p>
     * is called at jfxManager.createLayer
     *
     * @param jfxManager
     */
    @Override
    public void create(JFxManager jfxManager) {
        this.jFxManager = jfxManager;
        this.application = jfxManager.getApplication();
        created = true;
    }

    @Override
    public void restart() {

    }

    /**
     * Destroy this fxcontext
     */
    @Override
    public void destroy() {
        created = false;
    }

    /**
     * @return the jME3 Application
     */
    @Override
    public final Application getApplication() {
        return application;
    }

    /**
     * @return the manger which created this fxcontext
     */
    @Override
    public final JFxManager getJFxManager() {
        return jFxManager;
    }


    @Override
    public List<Layer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    protected void addLayer(Layer layer){
        layers.add(layer);
    }

    protected void removeLayer(Layer layer){
        layers.remove(layer);
    }

    public void pushFront(Layer layer){
        if(layers.size() <= 1){
            return;
        }
        layers.remove(layer);
        layers.add(0, layer);
    }

    public void pushBack(Layer layer){
        if(layers.size() <= 1){
            return;
        }
        layers.remove(layer);
        layers.add(layer);
    }

}
