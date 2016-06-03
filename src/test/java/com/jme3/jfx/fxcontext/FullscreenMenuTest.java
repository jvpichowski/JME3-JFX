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
import com.jme3.math.Vector2f;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/**
 * Created by jan on 13.05.16.
 */
public class FullscreenMenuTest {

    public static void main(String[] args){

        JFxManager jFxManager = new JFxManager();
        Application app = new SimpleApplication(jFxManager, new FlyCamAppState()) {
            @Override
            public void simpleInitApp() {
            }
        };



        jFxManager.onInit(() -> {
            app.getInputManager().setCursorVisible(true);

            jFxManager.onInit(() -> System.out.println("Init fx"));
            Stage stage = jFxManager.launch(FxContext.createContext(InputAdapter.createEatupAdapter(), app.getGuiViewPort()), layer -> {
                Stage primaryStage = ((Stage.Layer)layer).getStage();
                BorderPane root = new BorderPane();
                Scene scene = new Scene(root, 300, 250, Color.WHITE);

                MenuBar menuBar = new MenuBar();
                //menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
                root.setTop(menuBar);

                // File menu - new, save, exit
                Menu fileMenu = new Menu("File");
                MenuItem newMenuItem = new MenuItem("New");
                MenuItem saveMenuItem = new MenuItem("Save");
                MenuItem exitMenuItem = new MenuItem("Exit");
                exitMenuItem.setOnAction(actionEvent -> app.enqueue(() -> app.stop()));

                fileMenu.getItems().addAll(newMenuItem, saveMenuItem,
                        new SeparatorMenuItem(), exitMenuItem);

                Menu webMenu = new Menu("Web");
                CheckMenuItem htmlMenuItem = new CheckMenuItem("HTML");
                htmlMenuItem.setSelected(true);
                webMenu.getItems().add(htmlMenuItem);

                CheckMenuItem cssMenuItem = new CheckMenuItem("CSS");
                cssMenuItem.setSelected(true);
                webMenu.getItems().add(cssMenuItem);

                Menu sqlMenu = new Menu("SQL");
                ToggleGroup tGroup = new ToggleGroup();
                RadioMenuItem mysqlItem = new RadioMenuItem("MySQL");
                mysqlItem.setToggleGroup(tGroup);

                RadioMenuItem oracleItem = new RadioMenuItem("Oracle");
                oracleItem.setToggleGroup(tGroup);
                oracleItem.setSelected(true);

                sqlMenu.getItems().addAll(mysqlItem, oracleItem,
                        new SeparatorMenuItem());

                Menu tutorialManeu = new Menu("Tutorial");
                tutorialManeu.getItems().addAll(
                        new CheckMenuItem("Java"),
                        new CheckMenuItem("JavaFX"),
                        new CheckMenuItem("Swing"));

                sqlMenu.getItems().add(tutorialManeu);

                menuBar.getMenus().addAll(fileMenu, webMenu, sqlMenu);

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
