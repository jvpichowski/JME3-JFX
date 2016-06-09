package com.jme3.jfx.example.beginner;

import com.jme3.app.SimpleApplication;
import com.jme3.jfx.*;
import com.jme3.jfx.Utilities;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

/**
 * Created by jan on 09.06.16.
 */
public class C_HelloHUDButton {

    public static void main(String[] args){
        //create your SimpleApplication as always
        SimpleApplication application = new SimpleApplication() {
            @Override
            public void simpleInitApp() {

                Box b = new Box(1, 1, 1);                       // create cube shape
                Geometry geom = new Geometry("Box", b);         // create cube geometry from the shape
                Material mat = new Material(assetManager,
                        "Common/MatDefs/Misc/Unshaded.j3md");   // create a simple material
                mat.setColor("Color", ColorRGBA.Blue);          // set color of material to blue
                geom.setMaterial(mat);                          // set the cube's material
                rootNode.attachChild(geom);                     // make the cube appear in the scene

                //create a JFxManager
                JFxManager jFxManager = new JFxManager();
                //add a task to the JFxManager which will be called
                //when the JFxManager is ready
                //the task will be called on the jME3 Render Thread
                jFxManager.onInit(() -> initFx(jFxManager, mat));
                //don't forget to attach the JFxManager to the StateManager
                getStateManager().attach(jFxManager);
            }
        };
        application.start();
    }

    private static void initFx(JFxManager jFxManager, Material material){
        jFxManager.getApplication().getInputManager().setCursorVisible(true);
        //enable fx input listening
        jFxManager.beginInput();
        //create a configuration
        Configuration configuration = new Configuration();
        //define a render target
        //we want to render to fullscreen to the gui ViewPort
        configuration.setRenderSystem(
                RenderSystem.renderToViewPort(jFxManager.getApplication().getGuiViewPort()));
        //set a name so we know which geometries in the
        //scene graph belong to the RenderSystem
        configuration.setName("Java Fx HUD");

        //you have to convert the coordinates of the mouse from jME3 space to fx space
        configuration.setMouseInputConverter(MouseInputConverters.FullscreenInput);

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

            //at event change the color of the cube
            //don't forget enqueue the change to jME3
            Button b = new Button("BLUE");
            b.setOnMouseClicked(event -> jFxManager.getApplication().enqueue(
                    () -> material.setColor("Color", Utilities.convert(Color.BLUE))));
            root.getChildren().add(b);

            Button r = new Button("RED");
            r.setOnMouseClicked(event -> jFxManager.getApplication().enqueue(
                    () -> material.setColor("Color", Utilities.convert(Color.RED))));
            r.setTranslateY(50);
            root.getChildren().add(r);

            Button g = new Button("GREEN");
            g.setOnMouseClicked(event -> jFxManager.getApplication().enqueue(
                    () -> material.setColor("Color", Utilities.convert(Color.GREEN))));
            g.setTranslateY(100);
            root.getChildren().add(g);

            //enable when fx consumes mouse events
            //in this case fx consumes mouse events if the mouse is above a
            //fx element which is not invisible
            l.setInputConsumerMode(InputConsumerModes.StrictAlphaBased);
            //show the scene
            l.setScene(scene);
            l.show();
        });
    }
}
