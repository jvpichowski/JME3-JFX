package com.jme3.jfx;

import com.jme3.jfx.fxcontext.FxContext;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.scene.Scene;

import java.awt.*;
import java.util.function.BiPredicate;

/**
 * Created by jan on 13.05.16.
 *
 * replace by layer
 */
@Deprecated
public interface Stage {

    //	private FxContext fxcontext;
//
//	public BaseStage(FxContext fxcontext){
//		this.fxcontext = fxcontext;
//	}
//
//	public Scene getScene() {
//		return fxcontext.getScene();
//	}
//
//	public void setScene(final Scene newScene) {
//		fxcontext.setScene(newScene);
//	}

//    public Parent getRootNode() {
//		return this.rootNode;
//	}
//
//	public ObservableList<Node> getRootChildren() {
//		if (this.rootNode instanceof Group) {
//			return ((Group) this.rootNode).getChildren();
//		} else if (this.rootNode instanceof Pane) {
//			return ((Pane) this.rootNode).getChildren();
//		} else {
//			return FXCollections.emptyObservableList();
//		}
//	}

    @Deprecated
    class Layer implements com.jme3.jfx.Layer{
        @Override
        public void setInputConsumerMode(BiPredicate<com.jme3.jfx.Layer, Point> mode) {

        }

        private Stage stage;

        public Layer(Stage stage){
            this.stage = stage;
        }

        @Override
        public void toFront() {

        }

        @Override
        public void toBack() {

        }

        @Override
        public void setPosition(float x, float y) {

        }

        @Override
        public void setSize(float width, float height) {

        }

        @Override
        public void setScene(Scene scene) {
            stage.setScene(scene);
        }

        @Override
        public void loseFocus() {

        }

        @Override
        public void grabFocus() {

        }

        @Override
        public boolean hasFocus() {
            return stage.hasFocus();
        }

        @Override
        public void show() {

        }

        @Override
        public void close() {

        }

        @Override
        public float getX() {
            return 0;
        }

        @Override
        public float getY() {
            return 0;
        }

        @Override
        public int getWidth() {
            return stage.getWidth();
        }

        @Override
        public int getHeight() {
            return stage.getHeight();
        }

        @Override
        public void setTitle(String title) {

        }

        public Stage getStage(){
            return stage;
        }
    }

    void setScene(Scene scene);
    FxContext getContext();
    ReadOnlyIntegerProperty getWidthProperty();
    int getWidth();
    ReadOnlyIntegerProperty getHeightProperty();
    int getHeight();
    //but could only grab focus in fxcontext -> it should be seperated from fxlogic because it is not needed there to grab focus
    ReadOnlyBooleanProperty getFocusProperty();//property to add listeners and also jfx could add handlers
    boolean hasFocus(); //same as in fxcontext
}
