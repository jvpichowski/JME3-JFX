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
import com.jme3.jfx.fxcontext.input.InputAdapter;
import com.jme3.math.Vector2f;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.lwjgl.opengl.Display;

/**
 * Created by jan on 13.05.16.
 */
public class ContextMenuTest {

    public static void main(String[] args){


        JFxManager jFxManager = new JFxManager();
        Application app = new SimpleApplication(jFxManager, new FlyCamAppState()) {
            @Override
            public void simpleInitApp() {

                //in update:
//                if(Display.wasResized()) {
//                    int newWidth = Math.max(Display.getWidth(), 1);
//                    int newHeight = Math.max(Display.getHeight(), 1);
//                    reshape(newWidth, newHeight);
//                }

//                getContext().setSystemListener(new SystemListener() {
//                    @Override
//                    public void initialize() {
//
//                    }
//
//                    @Override
//                    public void reshape(int width, int height) {
//
//                    }
//
//                    @Override
//                    public void update() {
//
//                    }
//
//                    @Override
//                    public void requestClose(boolean esc) {
//
//                    }
//
//                    @Override
//                    public void gainFocus() {
//                        System.out.println("gain focus");
//                    }
//
//                    @Override
//                    public void loseFocus() {
//                        System.out.println("lose focus");
//                    }
//
//                    @Override
//                    public void handleError(String errorMsg, Throwable t) {
//
//                    }
//
//                    @Override
//                    public void destroy() {
//
//                    }
//                });
            }
        };



        jFxManager.onInit(() -> {
            app.getInputManager().setCursorVisible(true);

            jFxManager.onInit(() -> System.out.println("Init fx"));
            Stage stage = jFxManager.launch(FxContext.createContext(InputAdapter.createEatupAdapter(), app.getGuiViewPort()), primaryStage -> {
                BorderPane root = new BorderPane();
                Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight(), Color.WHITE);
                final ContextMenu contextMenu = new ContextMenu();
                MenuItem cut = new MenuItem("Cut");
                MenuItem copy = new MenuItem("Copy");
                MenuItem paste = new MenuItem("Paste");
                contextMenu.getItems().addAll(cut, copy, paste);
// fill pane with nodes, etc
// create fxcontext menu and menu items as above
                root.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
//                        if (event.isSecondaryButtonDown()) {

                            contextMenu.show(root, Display.getX()+event.getScreenX(), jFxManager.getScreenY(event.getScreenY()));
//                        }
                    }
                });

                primaryStage.setScene(scene);
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
