package com.jme3.jfx.example;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.jfx.FxApplication;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.base.Configuration;
import com.jme3.jfx.base.Context;
import com.jme3.jfx.base.MouseInputConverters;
import com.jme3.jfx.base.RenderSystem;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

/**
 * Created by jan on 06.06.16.
 */
public class Button {

    public static void main(String[] args){
        JFxManager jFxManager = new JFxManager();
        Application app = new SimpleApplication(jFxManager) {
            @Override
            public void simpleInitApp() {
                Box b = new Box(1, 1, 1);                                   // renderToFullscreen cube shape
                Geometry geom = new Geometry("Box", b);                     // renderToFullscreen cube geometry from the shape
                Material mat = new Material(getAssetManager(),
                        "Common/MatDefs/Misc/Unshaded.j3md");               // renderToFullscreen a simple material
                mat.setColor("Color", ColorRGBA.Blue);                      // set color of material to blue
                geom.setMaterial(mat);                                      // set the cube's material
                getRootNode().attachChild(geom);   // make the cube appear in the scene
            }
        };

        jFxManager.onInit(() -> {
            jFxManager.beginInput();
            Configuration config = new Configuration();

            config.setRenderSystem(RenderSystem.renderToViewPort(app.getGuiViewPort()));
            config.setMouseInputConverter(MouseInputConverters.FullscreenInput);
            config.setName("static layer fullscreen context");

            Context context = Context.create(config);

            jFxManager.launch(context, createFxApp());

            jFxManager.onClean(() -> context.destroy());
        });

        app.start();
    }

    private static FxApplication createFxApp(){
        return  primaryStage -> {
            Group root = new Group();
            Scene scene = new Scene(root);
            scene.setFill(new Color(0,0,0,0));

            javafx.scene.control.Button b = new javafx.scene.control.Button("click me");
            b.setOnMouseClicked(event -> b.setText(b.getText()+"."));
            root.getChildren().add(b);

            primaryStage.setTitle("Button Example");
            primaryStage.setScene(scene);
            primaryStage.show();
        };
    }
}
