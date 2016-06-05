package com.jme3.jfx.base.input;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.jfx.base.Context;

import java.util.function.Consumer;

/**
 * Last link calls input methods on context
 * should be moved to context impl
 *
 * Created by jan on 04.06.16.
 */
public class TranslationLink implements ContextInputLink{
    @Override
    public void setContext(Context context) {

    }

    @Override
    public void setOutput(ContextInputLink link) {
        throw new IllegalStateException("TranslationLink can't forward event!");
    }

    @Override
    public void apply(MouseMotionEvent evt, Consumer<MouseMotionEvent> callAfter) {

    }

    @Override
    public void apply(MouseButtonEvent evt, Consumer<MouseButtonEvent> callAfter) {
        //translate x to fx
        //translate y to fx
    }

    @Override
    public void apply(KeyInputEvent evt, Consumer<KeyInputEvent> callAfter) {

    }
}
