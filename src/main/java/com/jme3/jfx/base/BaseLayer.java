package com.jme3.jfx.base;

import com.jme3.jfx.Layer;
import javafx.scene.Scene;

/**
 * Created by jan on 05.06.16.
 */
abstract class BaseLayer implements Layer{

    private final FxContainer fxContainer;
    private final BaseContext context;

    private boolean hasFocus = false;
    private InputMode inputMode = InputMode.LEAK;

    public BaseLayer(BaseContext context, FxContainer fxContainer){
        this.context = context;
        this.fxContainer = fxContainer;
    }

    protected BaseContext getContext(){
        return context;
    }

    protected FxContainer getFxContainer(){
        return fxContainer;
    }


    @Override
    public void toFront() {
        context.pushFront(this);
    }

    @Override
    public void toBack() {
        context.pushBack(this);
    }

    @Override
    public void setScene(Scene scene){
        fxContainer.setScene(scene);
    }

    @Override
    public void loseFocus() {
        hasFocus = false;
    }

    @Override
    public void grabFocus() {
        hasFocus = true;
    }

    @Override
    public boolean hasFocus() {
        return hasFocus;
    }

    @Override
    public void setInputMode(InputMode mode) {
        this.inputMode = mode;
    }

    protected InputMode getInputMode(){
        return inputMode;
    }

    @Override
    public void close() {
        fxContainer.destroy();
    }

    @Override
    public int getWidth() {
        return fxContainer.getWidth();
    }

    @Override
    public int getHeight() {
        return fxContainer.getHeight();
    }

    @Override
    public void setTitle(String title) {
        fxContainer.setName(title);
    }
}
