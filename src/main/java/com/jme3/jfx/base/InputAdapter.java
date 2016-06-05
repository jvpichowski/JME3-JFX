package com.jme3.jfx.base;


import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;

import java.util.function.Consumer;

/**
 * Created by jan on 03.06.16.
 */
public interface InputAdapter {

    void apply(final MouseMotionEvent evt, Consumer<MouseMotionEvent> callAfter);

    void apply(final MouseButtonEvent evt, Consumer<MouseButtonEvent> callAfter);

    void apply(final KeyInputEvent evt, Consumer<KeyInputEvent> callAfter);


}
