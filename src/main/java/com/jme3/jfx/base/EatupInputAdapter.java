package com.jme3.jfx.base;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;

import java.util.function.Consumer;

/**
 * Created by jan on 03.06.16.
 */
class EatupInputAdapter extends BaseInputAdapter{

    public EatupInputAdapter(Context context){
        super(context);
    }

    @Override
    public void apply(MouseMotionEvent evt, Consumer<MouseMotionEvent> callAfter) {

    }

    @Override
    public void apply(MouseButtonEvent evt, Consumer<MouseButtonEvent> callAfter) {

    }

    @Override
    public void apply(KeyInputEvent evt, Consumer<KeyInputEvent> callAfter) {

    }
}
