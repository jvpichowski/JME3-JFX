package com.jme3.jfx.example.beginner;

import com.jme3.app.SimpleApplication;
import com.jme3.jfx.Configuration;
import com.jme3.jfx.Context;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.RenderSystem;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * Created by jan on 09.06.16.
 */
public class A_HelloHUD {

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
            }
        };
        application.start();
    }

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

            // load the image
            Image image = new Image("/com/jme3/jfx/example/jME3-logo.png");

            // simple displays ImageView the image as is
            ImageView iv = new ImageView();
            iv.setImage(image);

            root.getChildren().add(iv);

            //show the scene
            l.setScene(scene);
            l.show();
        });
    }

}
