package com.jme3.jfx.example;

import com.jme3.jfx.FxApplication;
import com.jme3.jfx.Layer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

/**
 * Created by jan on 09.06.16.
 */
public class ExampleJavaFxApplication implements FxApplication {

    @Override
    public void start(Layer primaryStage) throws Exception {
        //create the scene
        Group root = new Group();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        //add a button
        javafx.scene.control.Button b = new javafx.scene.control.Button("show frame");
        b.setOnMouseClicked(event -> {
            if(scene.getFill().equals(Color.TRANSPARENT)) {
                b.setText("remove frame");
                scene.setFill(Color.RED);
            }else{
                b.setText("show frame");
                scene.setFill(Color.TRANSPARENT);
            }

        });
        root.getChildren().add(b);

        //show the scene
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}
