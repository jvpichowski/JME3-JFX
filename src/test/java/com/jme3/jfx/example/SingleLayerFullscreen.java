package com.jme3.jfx.example;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Layer;
import com.jme3.jfx.base.Configuration;
import com.jme3.jfx.base.Context;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * Created by jan on 13.05.16.
 */
public class SingleLayerFullscreen {

    public static void main(String[] args){

        JFxManager jFxManager = new JFxManager();
        Application app = new SimpleApplication(jFxManager, new FlyCamAppState()) {
            @Override
            public void simpleInitApp() {
                //create background scene
                getStateManager().getState(FlyCamAppState.class).setEnabled(true);
                Box b = new Box(1, 1, 1);                       // create cube shape
                Geometry geom = new Geometry("Box", b);         // create cube geometry from the shape
                Material mat = new Material(assetManager,
                        "Common/MatDefs/Misc/Unshaded.j3md");   // create a simple material
                mat.setColor("Color", ColorRGBA.Blue);          // set color of material to blue
                geom.setMaterial(mat);                          // set the cube's material
                rootNode.attachChild(geom);                     // make the cube appear in the scene
            }
        };



        jFxManager.onInit(() -> {
            jFxManager.beginInput();
            app.getInputManager().setCursorVisible(true);

            jFxManager.onInit(() -> System.out.println("Init fx"));
            Layer layer = jFxManager.launch(Context.create(new Configuration().setViewPort(app.getGuiViewPort())),
                    primary -> {

                //create a scene
                Group root = new Group();
                Scene scene = new Scene(root);
                //print out every event which is not handled by the button
                scene.addEventHandler(Event.ANY, event -> {
                    System.out.println("Unhandled event: "+event);
                });
                //make scene transparent so you can see the cube in the background
                scene.setFill(new Color(0,0,0,0));

                // load the image
                Image image = new Image("/com/jme3/jfx/example/jME3-logo.png");

                // simple displays ImageView the image as is
                ImageView iv1 = new ImageView();
                iv1.setImage(image);

                Button b = new Button("klick me");
                b.setOnMouseClicked(event -> b.setText(b.getText()+"."));

                root.getChildren().add(iv1);
                root.getChildren().add(b);

                //show the scene
                primary.setScene(scene);
                primary.show();
            });

            //if you close the only layer the whole context will be closed
            jFxManager.onClean(() -> layer.close());
        });
        app.start();
    }
}
