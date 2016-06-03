package com.jme3.jfx.stage;

import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Stage;

/**
 * Created by jan on 18.05.16.
 */
public class FxWindow {

    protected JFxManager jFxManager;

    public FxWindow(Stage stage){
        super();
        this.jFxManager = stage.getContext().getJFxManager();
    }

}
