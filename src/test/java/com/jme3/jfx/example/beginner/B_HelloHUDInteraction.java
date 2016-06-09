package com.jme3.jfx.example.beginner;

import com.jme3.app.SimpleApplication;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.jfx.Configuration;
import com.jme3.jfx.Context;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.RenderSystem;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * Created by jan on 09.06.16.
 */
public class B_HelloHUDInteraction {

    public static void main(String[] args){
        //create your SimpleApplication as always
        SimpleApplication application = new SimpleApplication() {
            @Override
            public void simpleInitApp() {
                //create a JFxManager
                JFxManager jFxManager = new JFxManager();
                //add a task to the JFxManager which will be called
                //when the JFxManager is ready
                //the task will be called on the jME3 Render Thread
                jFxManager.onInit(() -> initFx(jFxManager));
                //don't forget to attach the JFxManager to the StateManager
                getStateManager().attach(jFxManager);


                Box b = new Box(1, 1, 1);                       // create cube shape
                Geometry geom = new Geometry("Box", b);         // create cube geometry from the shape
                Material mat = new Material(assetManager,
                        "Common/MatDefs/Misc/Unshaded.j3md");   // create a simple material
                mat.setColor("Color", ColorRGBA.Blue);          // set color of material to blue
                geom.setMaterial(mat);                          // set the cube's material
                rootNode.attachChild(geom);                     // make the cube appear in the scene
                geom.addControl(
                        new AbstractControl() {

                            float r = 0;

                            @Override
                            protected void controlUpdate(float tpf) {
                                spatial.rotate(0,tpf,0);
                                r+= tpf;
                                //enqueue your changes of the Fx Application with JFxManager.enqueue
                                jFxManager.enqueue(() -> text.setText("Rotation Counter: "+(r*FastMath.RAD_TO_DEG)/360));
                            }

                            @Override
                            protected void controlRender(RenderManager rm, ViewPort vp) {

                            }
                        });
            }
        };
        application.start();
    }

    private static Label text;

    private static void initFx(JFxManager jFxManager){
        //create a configuration
        Configuration configuration = new Configuration();
        //define a render target
        //we want to render to fullscreen to the gui ViewPort
        configuration.setRenderSystem(
                RenderSystem.renderToViewPort(jFxManager.getApplication().getGuiViewPort()));
        //set a name so we know which geometries in the
        //scene graph belong to the RenderSystem
        configuration.setName("Java Fx HUD");

        //create a context defined by the configuration
        Context context = Context.create(configuration);

        //launch your HUD
        //l is a Layer. It's similar to a Stage.
        jFxManager.launch(context, l -> {
            //create the scene
            Group root = new Group();
            Scene scene = new Scene(root);
            //Its important to set the fill transparent.
            //Otherwise we couldn't look through the HUD.
            scene.setFill(Color.TRANSPARENT);

            text = new Label("Rotation Counter: ");
            text.setTextFill(Color.BLUE);

            root.getChildren().add(text);

            //show the scene
            l.setScene(scene);
            l.show();
        });
    }
}
