package com.jme3.jfx;

import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.sun.javafx.embed.AbstractEvents;

import java.awt.event.KeyEvent;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by jan on 05.06.16.
 */
public final class InputAdapter {

    //should ne used on fx thread
    private BitSet keyStateSet = new BitSet(0xFF);
    private char[] keyCharSet = new char[Short.MAX_VALUE * 3];
    boolean[] mouseButtonState = new boolean[3];


    private List<InputListener> listeners = new LinkedList<>();

    InputAdapter() {
    }

    public int getEmbeddedModifiers() {
        int embeddedModifiers = 0;

        if (keyStateSet.get(KeyEvent.VK_SHIFT)) {
            embeddedModifiers |= AbstractEvents.MODIFIER_SHIFT;
        }

        if (keyStateSet.get(KeyEvent.VK_CONTROL)) {
            embeddedModifiers |= AbstractEvents.MODIFIER_CONTROL;
        }

        if (keyStateSet.get(KeyEvent.VK_ALT)) {
            embeddedModifiers |= AbstractEvents.MODIFIER_ALT;
        }

        if (keyStateSet.get(KeyEvent.VK_META)) {
            embeddedModifiers |= AbstractEvents.MODIFIER_META;
        }
        return embeddedModifiers;
    }

    public boolean getKeyState(int key) {
        return keyStateSet.get(key);
    }

    public char getKeyChar(int fx_keycode) {
        return keyCharSet[fx_keycode];
    }

    public boolean getMouseButtonState(int button) {
        if (button < 0 || button >= 3) {
            throw new IllegalArgumentException("Button has to be [0,1,2]");
        }
        return mouseButtonState[button];
    }

    public void register(InputListener listener) {
        listeners.add(listener);
    }

    public void unregister(InputListener listener) {
        listeners.remove(listener);
    }

    public void apply(final MouseMotionEvent evt, Consumer<MouseMotionEvent> callAfter) {
        int type = AbstractEvents.MOUSEEVENT_MOVED;
        int button = AbstractEvents.MOUSEEVENT_NONE_BUTTON;

        final int wheelRotation = (int) Math.round(evt.getDeltaWheel() / -120.0);

        if (wheelRotation != 0) {
            type = AbstractEvents.MOUSEEVENT_WHEEL;
            button = AbstractEvents.MOUSEEVENT_NONE_BUTTON;
        } else if (getMouseButtonState(0)) {
            button = AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
        } else if (getMouseButtonState(1)) {
            button = AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
        } else if (getMouseButtonState(2)) {
            button = AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
        }

        final int ftype = type;
        final int fbutton = button;
        listeners.forEach(l -> l.applyMouseInput(ftype, fbutton, wheelRotation, evt.getX(), evt.getY()));
    }

    public void apply(final MouseButtonEvent evt, Consumer<MouseButtonEvent> callAfter) {
        mouseButtonState[evt.getButtonIndex()] = evt.isPressed();


        int button;
        switch (evt.getButtonIndex()) {
            case 0:
                button = AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
                break;
            case 1:
                button = AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
                break;
            case 2:
                button = AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
                break;
            default:
                return;
        }

        // seems that generating mouse release without corresponding mouse pressed is causing problems in Scene.ClickGenerator

        int type;
        if (evt.isPressed()) {
            type = AbstractEvents.MOUSEEVENT_PRESSED;
        } else if (evt.isReleased()) {
            type = AbstractEvents.MOUSEEVENT_RELEASED;
            // and clicked ??
        } else {
            return;
        }
        listeners.forEach(l -> l.applyMouseInput(type, button, 0, evt.getX(), evt.getY()));
    }

    public void apply(final KeyInputEvent evt, Consumer<KeyInputEvent> callAfter) {
        final char keyChar = evt.getKeyChar();

        int fx_keycode = AwtKeyInput.convertJmeCode(evt.getKeyCode());
        if (fx_keycode > keyCharSet.length) {
            switch (keyChar) {
                case '\\':
                    fx_keycode = KeyEvent.VK_BACK_SLASH;
                    break;
                default:
                    return;
            }
        }

        final int ffx_keycode = fx_keycode;

        if (evt.isRepeating()) {
            final int eventType;
            if (fx_keycode == KeyEvent.VK_BACK_SPACE || fx_keycode == KeyEvent.VK_DELETE || fx_keycode == KeyEvent.VK_KP_UP || fx_keycode == KeyEvent.VK_KP_DOWN || fx_keycode == KeyEvent.VK_KP_LEFT || fx_keycode == KeyEvent.VK_KP_RIGHT
                    || fx_keycode == KeyEvent.VK_UP || fx_keycode == KeyEvent.VK_DOWN || fx_keycode == KeyEvent.VK_LEFT || fx_keycode == KeyEvent.VK_RIGHT) {
                eventType = AbstractEvents.KEYEVENT_PRESSED;
            } else {
                eventType = AbstractEvents.KEYEVENT_TYPED;
            }


            listeners.forEach(l -> l.applyKeyInput(eventType, ffx_keycode));
        } else if (evt.isPressed()) {
            this.keyCharSet[fx_keycode] = keyChar;
            this.keyStateSet.set(fx_keycode);

            listeners.forEach(l -> l.applyKeyInput(AbstractEvents.KEYEVENT_PRESSED, ffx_keycode));
            listeners.forEach(l -> l.applyKeyInput(AbstractEvents.KEYEVENT_TYPED, ffx_keycode));//move to last else?
        } else {
            this.keyStateSet.clear(fx_keycode);

            listeners.forEach(l -> l.applyKeyInput(AbstractEvents.KEYEVENT_RELEASED, ffx_keycode));
        }
    }

    /**
     * let layers and contexts listen for input. how to tranlate between pos of geometry?
     * called on java fx thread
     */
    public interface InputListener{

        /**
         *
         * @param eventType AbstractEvents.MOUSEEVENT_MOVED |
         *             AbstractEvents.MOUSEEVENT_WHEEL|
         *             AbstractEvents.MOUSEEVENT_PRESSED |
         *             AbstractEvents.MOUSEEVENT_RELEASED
         * @param button AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON |
         *               AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON |
         *               AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON |
         *               AbstractEvents.MOUSEEVENT_NONE_BUTTON
         * @param wheelRotation
         * @param jME_x screen x
         * @param jME_y screen y
         * @return true if consumed by context
         */
        boolean applyMouseInput(int eventType, int button, int wheelRotation, int jME_x, int jME_y);

        /**
         *
         * @param eventType AbstractEvents.KEYEVENT_PRESSED |
         *                  AbstractEvents.KEYEVENT_TYPED |
         *                  AbstractEvents.KEYEVENT_RELEASED
         * @param fx_keycode
         * @return true if consumed by context
         */
        boolean applyKeyInput(int eventType, int fx_keycode);

    }
}
