package com.jme3.jfx.example;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.jfx.FxApplication;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Layer;
import com.jme3.jfx.base.Configuration;
import com.jme3.jfx.base.Context;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * Created by jan on 06.06.16.
 */
public class StaticLayers {

    public static void main(String[] args){
        JFxManager jFxManager = new JFxManager();
        Application app = new SimpleApplication(jFxManager) {
            @Override
            public void simpleInitApp() {
                Box b = new Box(1, 1, 1);                                   // create cube shape
                Geometry geom = new Geometry("Box", b);                     // create cube geometry from the shape
                Material mat = new Material(getAssetManager(),
                        "Common/MatDefs/Misc/Unshaded.j3md");               // create a simple material
                mat.setColor("Color", ColorRGBA.Blue);                      // set color of material to blue
                geom.setMaterial(mat);                                      // set the cube's material
                getRootNode().attachChild(geom);   // make the cube appear in the scene
            }
        };

        jFxManager.onInit(() -> {
            Configuration config = new Configuration();
            config.setViewPort(app.getGuiViewPort());
            config.setSingleLayer(false);
            config.setStaticLayers(true);
            config.setName("static layer fullscreen context");

            Context context = Context.create(config);

            Layer l0 = jFxManager.launch(context, createFxApp(0));
            context.createLayer(createFxApp(1));
            context.createLayer(createFxApp(2));
            context.createLayer(createFxApp(4));
            context.createLayer(createFxApp(3));
            l0.toFront();
        });
        app.start();
    }

    private static FxApplication createFxApp(final int num){
        return  primaryStage -> {
            //Add a scene
            Group root = new Group();
            Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
            scene.setFill(new Color(0,0,0,0)); //transparent

            // load the image
            Image image = new Image("http://www.be.unsw.edu.au/sites/all/modules/media_gallery/images/empty_gallery.png");

            // simple displays ImageView the image as is
            ImageView iv = new ImageView();
            iv.setImage(image);
            iv.setX(num * 50);

            root.getChildren().add(iv);

            //show the stage
            primaryStage.setTitle("FxApp");
            primaryStage.setScene(scene);
            primaryStage.show();
        };
    }
}
