package com.jme3.jfx.example.advanced;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.jfx.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Created by jan on 06.06.16.
 */
public class MultipleLayers {

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

//            config.setRenderSystem(RenderSystem.renderToFullscreen(
//                    app.getGuiViewPort().getCamera().getWidth(),
//                    app.getGuiViewPort().getCamera().getHeight()));

            config.setRenderSystem(RenderSystem.renderToViewPort(app.getGuiViewPort()));

//            config.setRenderSystem(RenderSystem.renderToTexture(200, 200, texture2D -> {
//                Geometry geom = new Geometry("Java Fx Quad", new Quad(200, 200, true));
//                Material material = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//                material.setTexture("ColorMap", texture2D);
//                material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
//                geom.setQueueBucket(RenderQueue.Bucket.Gui);
//                geom.setMaterial(material);
//                ((SimpleApplication)app).getGuiNode().attachChild(geom);
//            }));

            config.setMouseInputConverter(MouseInputConverters.FullscreenInput);
            config.setName("static layer fullscreen context");

            Context context = Context.create(config);

            Layer l0 = jFxManager.launch(context, createFxApp(0));
            Layer l1 = context.createLayer(createFxApp(1));
            context.createLayer(createFxApp(2));
            context.createLayer(createFxApp(3));
            context.createLayer(createFxApp(4));
            //change ordering
//            l0.toFront();
//            l1.toBack();

            jFxManager.onClean(context::destroy);
        });
        app.start();
    }

    private static FxApplication createFxApp(final int num){
        return  primaryStage -> {
            //Add a scene
            Group root = new Group();
            Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
            scene.setFill(Color.TRANSPARENT);
            scene.addEventHandler(Event.ANY, event -> {
                System.out.println("Unhandled event at " +num+": "+event);
            });

            // load the image
            Image image = new Image("/com/jme3/jfx/example/jME3-logo.png");

            // simple displays ImageView the image as is
            ImageView iv = new ImageView();
            iv.setImage(image);
            iv.setX(num * 50);

            //push layer to front if the image gets clicked
            iv.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> primaryStage.toFront());

            root.getChildren().add(iv);

            //show the stage
            primaryStage.setInputConsumerMode(InputConsumerModes.StrictAlphaBased);
            primaryStage.setTitle("FxApp");
            primaryStage.setScene(scene);
            primaryStage.show();
        };
    }
}
