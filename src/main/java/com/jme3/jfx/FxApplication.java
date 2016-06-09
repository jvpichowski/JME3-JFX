package com.jme3.jfx;

/**
 *
 * This interface must be implemented to create a new
 * Java Fx instance. It is launched via the JFxManager.
 *
 */
public interface FxApplication {

    /**
     * @throws IllegalStateException because it is not implemented yet.
     * @param path
     * @return
     */
    static FxApplication fromFxml(final String path){
        throw new IllegalStateException("Not implemented yet!");
    }

    void start(Layer primaryStage) throws Exception;

}
