package com.jme3.jfx.example.advanced;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.jfx.*;
import com.jme3.jfx.example.ExampleJavaFxApplication;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;

/**
 * Created by jan on 08.06.16.
 */
public class CursorClick {

    public static void main(String[] args){
        //create the jME3 app
        JFxManager jFxManager = new JFxManager();
        SimpleApplication app = new SimpleApplication(jFxManager, new FlyCamAppState()) {
            @Override
            public void simpleInitApp() {
                Box b = new Box(1, 1, 1);                                   // renderToFullscreen cube shape
                Geometry geom = new Geometry("Box", b);                     // renderToFullscreen cube geometry from the shape
                Material mat = new Material(getAssetManager(),
                        "Common/MatDefs/Misc/Unshaded.j3md");               // renderToFullscreen a simple material
                mat.setColor("Color", ColorRGBA.Blue);                      // set color of material to blue
                geom.setMaterial(mat);                                      // set the cube's material
                getRootNode().attachChild(geom);                            // make the cube appear in the scene
                //move cube behind fx quad
                geom.move(0,0,-10);
            }
        };
        //wait until the JFxManager is initialized until we attach our scene
        jFxManager.onInit(() -> {
            app.getStateManager().getState(FlyCamAppState.class).setEnabled(true);
            app.getInputManager().setCursorVisible(true);
            //enable input
            jFxManager.beginInput();

            Configuration config = new Configuration();

            //create the geometry to which the fx context should be rendered
            Geometry geometry = new Geometry("fx geometry", new Quad(2, 2, false));
            app.getRootNode().attachChild(geometry);

            //create a render target
            final int width = 100;
            final int height = 100;
            config.setRenderSystem(RenderSystem.renderToGeometry(width, height, geometry));

            //create a custom input converter
            //this converter casts a ray from the 2d click to the geometry
            config.setMouseInputConverter(MouseInputConverters.FullscreenCursorToGeometry(app.getCamera(), geometry, width, height, app.getRootNode()));

            //create the context
            Context context = Context.create(config);

            jFxManager.launch(context, new ExampleJavaFxApplication());
        });

        //don't forget to start jME3
        app.start();
    }

}
