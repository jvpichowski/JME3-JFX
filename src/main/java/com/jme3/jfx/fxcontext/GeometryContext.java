package com.jme3.jfx.fxcontext;

import com.jme3.jfx.JFxManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.jfx.input.InputAdapter;

/**
 * Created by jan on 14.05.16.
 */
class GeometryContext extends BaseContext {

    protected Geometry geom;

    public GeometryContext(InputAdapter inputAdapter, Geometry geom, int width, int height) {
        super(inputAdapter, width, height);
        this.geom = geom;
    }

    @Override
    public void create(JFxManager jFxManager){
        super.create(jFxManager);
//        this.fullscreenSupport = true;



        //installSceneAccessorHack();

        final Material mat = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", getTexture());
        geom.setMaterial(mat);
    }

    @Override
    public void destroy() {
        geom.removeFromParent();
        super.destroy();
    }
}
