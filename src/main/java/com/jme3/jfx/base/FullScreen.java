package com.jme3.jfx.base;

import com.jme3.jfx.FxApplication;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Layer;
import com.jme3.jfx.input.InputAdapter;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation
 *
 * Default context for fullscreen application.
 *
 * Created by jan on 16.05.16.
 */
final class FullScreen extends BaseContext{



    private ViewPort viewPort;
    private Geometry geom;
    private Material material;

    private LayerImpl layer;

    FullScreen(ViewPort viewPort){
        this.viewPort = viewPort;
    }

    @Override
    public Layer createLayer(FxApplication application){
        if(layer != null){
            throw new IllegalStateException("This fxcontext doesn't allow multiple layers");
        }
        FxContainer fxContainer = new FxContainer(viewPort.getCamera().getWidth(), viewPort.getCamera().getHeight());
        fxContainer.create(this);
        layer = new LayerImpl(fxContainer);
        try {
            application.start(layer);
        } catch (Exception e) {
            layer = null;
            e.printStackTrace();
            return null;
        }
        return layer;
    }

    @Override
    public List<Layer> getLayers() {
        List<Layer> result = new ArrayList<>();
        result.add(layer);
        return result;
    }

    @Override
    public void create(JFxManager jFxManager){
        super.create(jFxManager);
    }



    @Override
    public void destroy() {
        geom.removeFromParent();
        if(layer != null){
            layer.fxContainer.destroy();
        }
        super.destroy();
    }


    //******************************************************************//
    // Input management                                                 //
    //******************************************************************//

    /**
     * @return the InputAdapter
     */
    @Override
    public InputAdapter getInputAdapter() {
        return null;
    }

    /**
     * grab the focus to this scene
     */
    @Override
    public void grabFocus() {

    }

    /**
     * remove the focus from this scene
     */
    @Override
    public void loseFocus() {

    }

    /**
     * @return true if the focus is grabbed otherwise false
     */
    @Override
    public boolean hasFocus() {
        return false;
    }




    private final class LayerImpl implements Layer {

        private final FxContainer fxContainer;

        public LayerImpl(FxContainer fxContainer){
            this.fxContainer = fxContainer;
        }

        @Override
        public void setScene(Scene scene){
            fxContainer.setScene(scene);
        }

        @Override
        public void show() {
            getApplication().enqueue(() -> {
                geom = new Geometry(getName(), new Quad(fxContainer.getWidth(), fxContainer.getHeight(), true));

                material = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                material.setTexture("ColorMap", fxContainer.getTexture());
                material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                geom.setQueueBucket(RenderQueue.Bucket.Gui);
                geom.setMaterial(material);
                ((Node) viewPort.getScenes().get(0)).attachChild(geom);
            });
        }

        @Override
        public void close() {
            geom.removeFromParent();
            fxContainer.destroy();
            FullScreen.this.layer = null;
        }

        @Override
        public void setTitle(String title) {
            fxContainer.setName(title);
        }

        @Override
        public void toFront() {
            //nothing to do because there is only one layer
        }

        @Override
        public void toBack() {
            //nothing to do because there is only one layer
        }

        @Override
        public void setPosition(float x, float y) {
            throw new IllegalStateException("This layer can't be moved");
        }

        @Override
        public void setSize(float width, float height) {
            throw new IllegalStateException("This layer can't be resized");
        }

        @Override
        public int getWidth() {
            return fxContainer.getWidth();
        }

        @Override
        public int getHeight() {
            return fxContainer.getHeight();
        }
    }
}