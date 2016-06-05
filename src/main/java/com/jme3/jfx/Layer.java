package com.jme3.jfx;

import javafx.scene.Scene;

/**
 * Created by jan on 26.05.16.
 *
 * A Layer is the frame of every FxApplication.
 * Every Layer belongs to a Context. The Context defines also a frame.
 * A Context could hold multiple Layers. Thus Layers could be ordered
 * by toFront() or toBack(). The layer-bounds could be outside of the
 * Context frame. Only the part of the layer inside the Context frame
 * is drawn and could react to input events.
 *
 */
public interface Layer {

    void toFront();
    void toBack();

    void setPosition(float x, float y);
    void setSize(float width, float height);

    void setScene(Scene scene);

    enum InputMode{
        LEAK,
        EAT_UP,
        ALPHA,
        FX_BASED
    }

    void loseFocus();
    void grabFocus();
    boolean hasFocus();

    void setInputMode(InputMode mode);

    //TODO add config param that shows the layer at creating to save a frame
    void show();
    void close();

    int getWidth();
    int getHeight();

    void setTitle(String title);

}
