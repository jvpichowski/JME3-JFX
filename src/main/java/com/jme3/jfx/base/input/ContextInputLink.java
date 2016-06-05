package com.jme3.jfx.base.input;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.jfx.base.Context;

import java.util.function.Consumer;

/**
 * Created by jan on 04.06.16.
 */
public interface ContextInputLink {

    void setContext(Context context);

    void setOutput(ContextInputLink link);

    void apply(final MouseMotionEvent evt, Consumer<MouseMotionEvent> callAfter);

    void apply(final MouseButtonEvent evt, Consumer<MouseButtonEvent> callAfter);

    void apply(final KeyInputEvent evt, Consumer<KeyInputEvent> callAfter);


}
