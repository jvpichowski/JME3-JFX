package com.jme3.jfx;

import javafx.scene.Scene;

import java.awt.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * A Layer is the frame of every FxApplication.
 * Every Layer belongs to a Context. The Context defines also a frame.
 * A Context could hold multiple Layers. Thus Layers could be ordered
 * by toFront() or toBack(). The layer-bounds could be outside of the
 * Context frame. Only the part of the layer inside the Context frame
 * is drawn and could react to input events.
 *
 */
public interface Layer {

    /**
     * Moves this layer to the front of the context
     */
    void toFront();

    /**
     * Moves this layer to the background of the context
     */
    void toBack();

    void setPosition(float x, float y);
    void setSize(float width, float height);

    /**
     *
     * @param scene
     */
    void setScene(Scene scene);

    /**
     * Ungrabs the focus from this layer.
     * No more key events are handled.
     */
    void loseFocus();

    /**
     * Grabs the focus to this layer.
     * Key events will be handled.
     */
    void grabFocus();

    /**
     * Key events are handled if the layer has the focus.
     *
     * @return
     */
    boolean hasFocus();

    /**
     * Defines when mouse inputs are handled
     *
     * @param mode
     */
    void setInputConsumerMode(BiPredicate<Layer, Point> mode);

    //TODO add config param that shows the layer at creating to save a frame
    void show();

    /**
     * Close this layer. If it belongs to a
     * forced single layer context the context
     * will be destroyed to.
     */
    void close();

    /**
     *
     * @return
     */
    int getWidth();
    int getHeight();
    float getX();
    float getY();

    /**
     * Sets the title of this layer. You can't see this
     * title. It is only useful for debugging.
     * @param title
     */
    void setTitle(String title);

}
