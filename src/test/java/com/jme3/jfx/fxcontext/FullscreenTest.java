package com.jme3.jfx.fxcontext;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Stage;
import com.jme3.jfx.input.InputAdapter;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
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
public class FullscreenTest {

    public static void main(String[] args){

        JFxManager jFxManager = new JFxManager();
        Application app = new SimpleApplication(jFxManager, new FlyCamAppState()) {
            @Override
            public void simpleInitApp() {
                getStateManager().getState(FlyCamAppState.class).setEnabled(true);
                Box b = new Box(1, 1, 1); // create cube shape
                Geometry geom = new Geometry("Box", b);  // create cube geometry from the shape
                Material mat = new Material(assetManager,
                        "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
                mat.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
                geom.setMaterial(mat);                   // set the cube's material
                rootNode.attachChild(geom);              // make the cube appear in the scene

//                Box cube2Mesh = new Box( 1f,1f,1f);
//                Geometry cube2Geo = new Geometry("window frame", cube2Mesh);
//                Material cube2Mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//                cube2Mat.setColor("Color", new ColorRGBA(1,0,0,0.5f));
//                cube2Mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);  // activate transparency
//                cube2Geo.setQueueBucket(RenderQueue.Bucket.Transparent);
//                cube2Geo.setMaterial(cube2Mat);
//                rootNode.attachChild(cube2Geo);
            }
        };



        jFxManager.onInit(() -> {
            app.getInputManager().setCursorVisible(true);

            jFxManager.onInit(() -> System.out.println("Init fx"));
            Stage stage = jFxManager.launch(FxContext.createContext(InputAdapter.createEatupAdapter(), app.getGuiViewPort()), l -> {
                Stage s = ((Stage.Layer)l).getStage();
                // load the image
                Image image = new Image("https://i.ytimg.com/vi/x73gO38mIQw/hqdefault.jpg");

                // simple displays ImageView the image as is
                ImageView iv1 = new ImageView();
                iv1.setImage(image);

                Group root = new Group();
                Scene scene = new Scene(root);
                scene.addEventHandler(Event.ANY, event -> {
                    System.out.println("Unhandled event: "+event);
                });
                scene.setFill(new Color(0,0,0,0));
                Button b = new Button("klick me");
                b.setOnMouseClicked(event -> b.setText(b.getText()+"1"));
                root.getChildren().add(b);

                s.setScene(scene);
            });

            jFxManager.onClean(() -> stage.getContext().destroy());
            jFxManager.onClean(() -> System.out.println("Clean fx"));

            //app.getCamera().setLocation(new Vector3f(10, 10, 15));


            app.getInputManager().addMapping("LeftMouse", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
            app.getInputManager().addMapping("KeyA", new KeyTrigger(KeyInput.KEY_A));
            app.getInputManager().addListener(new ActionListener() {
                @Override
                public void onAction(String name, boolean isPressed, float tpf) {
                    CollisionResults results = new CollisionResults();

                    Vector2f click2d = app.getInputManager().getCursorPosition();

                    stage.getContext().getInputAdapter().onMouseButtonEvent(InputAdapter.translate(
                            stage, MouseInput.BUTTON_LEFT, isPressed, app.getTimer().getTime(), click2d.getX(), click2d.getY()),
                            k -> System.out.println(k.isConsumed() ? "Consumed" : "Not consumed"));


                }
            }, "LeftMouse");
        });
        app.start();
    }
}
