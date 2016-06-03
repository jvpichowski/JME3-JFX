package com.jme3.jfx;

/**
 *
 * This interface must be implemented to create a new
 * Java Fx instance. It is launched via the JFxManager.
 *
 * Created by jan on 28.04.16.
 */
public interface FxApplication {

    static FxApplication fromFxml(final String path){
        throw new IllegalStateException("Not implemented yet!");
    }

    void start(Layer primaryStage) throws Exception;

}
