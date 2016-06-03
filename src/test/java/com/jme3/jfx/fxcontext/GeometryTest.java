package com.jme3.jfx.fxcontext;

import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Stage;
import com.jme3.jfx.fxcontext.input.InputAdapter;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
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
public class GeometryTest {

    public static void main(String[] args){

        JFxManager jFxManager = new JFxManager();
        Application app = new SimpleApplication(jFxManager, new FlyCamAppState()) {
            @Override
            public void simpleInitApp() {
                getStateManager().getState(FlyCamAppState.class).setEnabled(true);
            }
        };

        jFxManager.onInit(() -> {
            app.getInputManager().setCursorVisible(true);

            //jFxManager.createLayer(FxContext.createContext(geom,w,h), FxApplication.fromFxml(path));
            final Geometry screen = new Geometry("Screen1", new Quad(20, 20, true));
//            final Geometry screen = new Geometry("Screen1", new Sphere(100,100,10));

            jFxManager.onInit(() -> System.out.println("Init fx"));
            Stage stage = jFxManager.launch(FxContext.createContext(InputAdapter.createAlphaAdapter(), screen, 200, 200), l -> {
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
                scene.setFill(new Color(0, 0, 0, 0));
                Button b = new Button("klick me");
                b.setOnMouseClicked(event -> b.setText(b.getText()+"1"));
                root.getChildren().add(b);

                s.setScene(scene);
            });

            jFxManager.onClean(() -> stage.getContext().destroy());
            jFxManager.onClean(() -> System.out.println("Clean fx"));

            screen.setLocalTranslation(0, 0, 0);
            ((SimpleApplication)app).getRootNode().attachChild(screen);
            app.getCamera().setLocation(new Vector3f(10, 10, 15));


            app.getInputManager().addMapping("LeftMouse", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
            app.getInputManager().addMapping("KeyA", new KeyTrigger(KeyInput.KEY_A));
            app.getInputManager().addListener(new ActionListener() {
                @Override
                public void onAction(String name, boolean isPressed, float tpf) {
                    CollisionResults results = new CollisionResults();

                    Vector2f click2d = app.getInputManager().getCursorPosition();
                    Vector3f click3d = app.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                    Vector3f dir = app.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
                    Ray ray = new Ray(click3d, dir);
                    ((SimpleApplication)app).getRootNode().collideWith(ray, results);

                    CollisionResult collision = results.getClosestCollision();
                    if (collision == null) {
                        return;
                    }
                    Geometry geom = collision.getGeometry();
                    if (geom != screen) {
                        return;
                    }
                    stage.getContext().getInputAdapter().onMouseButtonEvent(InputAdapter.translate(
                            stage, MouseInput.BUTTON_LEFT, isPressed, app.getTimer().getTime(), collision),
                        k -> System.out.println(k.isConsumed() ? "Consumed" : "Not consumed"));


                }
            }, "LeftMouse");
        });
        app.start();
    }
}
