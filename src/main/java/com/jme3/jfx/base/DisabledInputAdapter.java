package com.jme3.jfx.base;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;

import java.util.function.Consumer;

/**
 * Created by jan on 03.06.16.
 */
final class DisabledInputAdapter implements InputAdapter {

    private Context context;

    public DisabledInputAdapter(Context context){
        this.context = context;
    }

    @Override
    public void apply(MouseMotionEvent evt, Consumer<MouseMotionEvent> callAfter) {
        context.getApplication().enqueue(() -> callAfter.accept(evt));
    }

    @Override
    public void apply(MouseButtonEvent evt, Consumer<MouseButtonEvent> callAfter) {
        context.getApplication().enqueue(() -> callAfter.accept(evt));
    }

    @Override
    public void apply(KeyInputEvent evt, Consumer<KeyInputEvent> callAfter) {
        context.getApplication().enqueue(() -> callAfter.accept(evt));
    }
}
