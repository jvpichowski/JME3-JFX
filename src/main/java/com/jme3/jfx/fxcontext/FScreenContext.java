package com.jme3.jfx.fxcontext;

import com.jme3.jfx.JFxManager;
import com.jme3.jfx.input.InputAdapter;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 * Created by jan on 16.05.16.
 */
public class FScreenContext extends BaseContext {

    protected ViewPort viewPort;
    protected Geometry geom;

    public FScreenContext(InputAdapter inputAdapter, ViewPort viewPort){
        super(inputAdapter, viewPort.getCamera().getWidth(), viewPort.getCamera().getHeight());
        this.viewPort = viewPort;
        setName("FullScreenContext");
    }

    @Override
    public void create(JFxManager jFxManager){
        super.create(jFxManager);
        geom = new Geometry(getName(), new Quad(viewPort.getCamera().getWidth(), viewPort.getCamera().getHeight(), true));

        final Material mat = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", getTexture());
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setQueueBucket(RenderQueue.Bucket.Gui);
        geom.setMaterial(mat);
        ((Node)viewPort.getScenes().get(0)).attachChild(geom);
    }



    @Override
    public void destroy() {
        geom.removeFromParent();
        super.destroy();
    }

}
