package com.jme3.jfx.base.input;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.jfx.base.Context;

import java.util.function.Consumer;

/**
 * Created by jan on 04.06.16.
 */
public final class ContextRayCheck implements ContextInputLink {

    private Context context;
    private ContextInputLink out;

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void setOutput(ContextInputLink link) {
        this.out = link;
    }

    @Override
    public void apply(MouseMotionEvent evt, Consumer<MouseMotionEvent> callAfter) {
        //if no geometry in front
        out.apply(evt, callAfter);
    }

    @Override
    public void apply(MouseButtonEvent evt, Consumer<MouseButtonEvent> callAfter) {

    }

    @Override
    public void apply(KeyInputEvent evt, Consumer<KeyInputEvent> callAfter) {

    }
}
