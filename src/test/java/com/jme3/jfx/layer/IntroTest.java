package com.jme3.jfx.layer;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.base.Configuration;
import com.jme3.jfx.base.Context;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;

/**
 * Created by jan on 17.05.16.
 */
public class IntroTest {

    public static void main(String[] args){
        JFxManager jFxManager = new JFxManager();
        Application app = new SimpleApplication(jFxManager) {
            @Override
            public void simpleInitApp() {
                Box b = new Box(1, 1, 1); // create cube shape
                Geometry geom = new Geometry("Box", b);  // create cube geometry from the shape
                Material mat = new Material(assetManager,
                        "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
                mat.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
                geom.setMaterial(mat);                   // set the cube's material
                rootNode.attachChild(geom);              // make the cube appear in the scene

            }
        };
        jFxManager.onInit(() -> {
            jFxManager.launch(Context.create(new Configuration().setViewPort(app.getGuiViewPort())),
                primaryStage -> {
                    //Add a scene
                    Group root = new Group();
                    Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
                    scene.setFill(new Color(0,0,0,1));
                    //scene.setFill(new Color(0,0,0,0));

                    final Media media = new Media("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
                    MediaPlayer player = new MediaPlayer(media);
                    player.setOnEndOfMedia(() -> app.enqueue(() -> primaryStage.close()));
                    player.play();

                    //This mediaView is added to a Pane
                    MediaView mediaView = new MediaView(player);

                    //scale media to fill the stage
                    mediaView.setFitWidth(primaryStage.getWidth());

                    //center mediaView on screen. Ratio of video is 1:2,5
                    mediaView.setTranslateY(((double)primaryStage.getHeight()-(1d/2.5d*(double)primaryStage.getWidth()))/2d);
                    ((Group)scene.getRoot()).getChildren().add(mediaView);

                    //show the stage
                    primaryStage.setTitle("Media Player");
                    primaryStage.setScene(scene);
                    primaryStage.show();
                });
        });
        app.start();
    }
}
