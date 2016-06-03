package com.jme3.jfx.input;

import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.jfx.fxcontext.FxContext;
import com.sun.glass.ui.Pixels;
import com.sun.javafx.embed.AbstractEvents;

import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.function.Consumer;

/**
 * Created by jan on 14.05.16.
 */
public final class AlphaInputAdapter implements InputAdapter {


    private FxContext context;

    private BitSet keyStateSet = new BitSet(0xFF);
    private char[] keyCharSet = new char[Short.MAX_VALUE * 3];
    boolean[] mouseButtonState = new boolean[3];

    private int alphaByteOffset = 0;

    private int alphaLimit = 0;

    AlphaInputAdapter(){}

    AlphaInputAdapter(int alphaLimit){
        this.alphaLimit = alphaLimit;
    }

    /**
     * >= 256 works like EatupAdapter
     * @param alphaLimit 0 cosumes only full transparent, >= 256 consumes all, < 0 consumes nothing
     */
    public void setAlphaLimit(int alphaLimit){
        this.alphaLimit = alphaLimit;
    }

    @Override
    public void setContext(FxContext context) {
        this.context = context;
        context.getJFxManager().enqueue(() -> {
            // TODO 3.1: use Format.ARGB8 and Format.BGRA8 and remove used of exchangeData, fx2jme_ARGB82ABGR8,...
            switch (Pixels.getNativeFormat()) {
                case Pixels.Format.BYTE_ARGB:
                    alphaByteOffset = 0;
                    break;
                case Pixels.Format.BYTE_BGRA_PRE:
                    alphaByteOffset = 3;
                    break;
                default:
                    alphaByteOffset = 0;
                    break;
            };
        });
    }

    @Override
    public void onMouseMotionEvent(final MouseMotionEvent evt, Consumer<MouseMotionEvent> callAfter) {

        if (context.getSceneInterface() == null) {
            callAfter.accept(evt);
            return;
        }
        final int x = evt.getX();
        final int y = Math.round(context.getStage().getHeight()) - evt.getY();

        final boolean covered = isCovered(x, y);
        if (covered) {
            evt.setConsumed();
        }
        context.getJFxManager().enqueue(() -> {

            // not sure if should be grabbing focus on mouse motion event
            // grabFocus();

            int type = AbstractEvents.MOUSEEVENT_MOVED;
            int button = AbstractEvents.MOUSEEVENT_NONE_BUTTON;

            final int wheelRotation = (int) Math.round(evt.getDeltaWheel() / -120.0);

            if (wheelRotation != 0) {
                type = AbstractEvents.MOUSEEVENT_WHEEL;
                button = AbstractEvents.MOUSEEVENT_NONE_BUTTON;
            } else if (mouseButtonState[0]) {
                button = AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
            } else if (mouseButtonState[1]) {
                button = AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
            } else if (mouseButtonState[2]) {
                button = AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
            }

            final int ftype = type;
            final int fbutton = button;
            /**
             * ensure drag and drop is handled before the mouse release event fires
             */

            context.getSceneInterface().mouseEvent(ftype, fbutton, mouseButtonState[0], mouseButtonState[1], mouseButtonState[2], x, y,
                    x, y, keyStateSet.get(KeyEvent.VK_SHIFT),
                    keyStateSet.get(KeyEvent.VK_CONTROL), keyStateSet.get(KeyEvent.VK_ALT), keyStateSet.get(KeyEvent.VK_META), wheelRotation, false);

            context.getApplication().enqueue(() -> callAfter.accept(evt));
        });
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt, Consumer<MouseButtonEvent> callAfter) {
        // TODO: Process events in separate thread ?
        if (context.getSceneInterface() == null) {
            callAfter.accept(evt);
            return;
        }
        final int x = evt.getX();
        final int y =  Math.round(context.getStage().getHeight()) - evt.getY();

        final boolean covered = isCovered(x, y);
        if (!covered) {
            context.loseFocus();
        } else {
            evt.setConsumed();
            context.grabFocus();
        }

        context.getJFxManager().enqueue(() -> {

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

            mouseButtonState[evt.getButtonIndex()] = evt.isPressed();

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

            context.getSceneInterface().mouseEvent(type, button, mouseButtonState[0], mouseButtonState[1], mouseButtonState[2], x, y,
                    x, y, keyStateSet.get(KeyEvent.VK_SHIFT),
                    keyStateSet.get(KeyEvent.VK_CONTROL), keyStateSet.get(KeyEvent.VK_ALT), keyStateSet.get(KeyEvent.VK_META), 0,
                    button == AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON);

            context.getApplication().enqueue(() -> callAfter.accept(evt));

        });
    }

    @Override
    public void onKeyEvent(KeyInputEvent evt, Consumer<KeyInputEvent> callAfter) {

        if (context.getSceneInterface() == null) {
            callAfter.accept(evt);
            return;
        }

        final char keyChar = evt.getKeyChar();

        int fxKeycode = AwtKeyInput.convertJmeCode(evt.getKeyCode());

        final int keyState = this.retrieveKeyState();
        if (fxKeycode > this.keyCharSet.length) {
            switch (keyChar) {
                case '\\':
                    fxKeycode = java.awt.event.KeyEvent.VK_BACK_SLASH;
                    break;
                default:
                    return;
            }
        }
        if (context.hasFocus()) {
            evt.setConsumed();
        }

        if (evt.isRepeating()) {
            final char x = this.keyCharSet[fxKeycode];

            final int eventType;
            if (fxKeycode == KeyEvent.VK_BACK_SPACE || fxKeycode == KeyEvent.VK_DELETE || fxKeycode == KeyEvent.VK_KP_UP || fxKeycode == KeyEvent.VK_KP_DOWN || fxKeycode == KeyEvent.VK_KP_LEFT || fxKeycode == KeyEvent.VK_KP_RIGHT
                    || fxKeycode == KeyEvent.VK_UP || fxKeycode == KeyEvent.VK_DOWN || fxKeycode == KeyEvent.VK_LEFT || fxKeycode == KeyEvent.VK_RIGHT) {
                eventType = AbstractEvents.KEYEVENT_PRESSED;
            } else {
                eventType = AbstractEvents.KEYEVENT_TYPED;
            }

            if (context.hasFocus()) {
                context.getSceneInterface().keyEvent(eventType, fxKeycode, new char[] { x }, keyState);
            }
        } else if (evt.isPressed()) {
            this.keyCharSet[fxKeycode] = keyChar;
            this.keyStateSet.set(fxKeycode);
            if (context.hasFocus()) {
                context.getSceneInterface().keyEvent(AbstractEvents.KEYEVENT_PRESSED, fxKeycode, new char[] { keyChar }, keyState);
                context.getSceneInterface().keyEvent(AbstractEvents.KEYEVENT_TYPED, fxKeycode, new char[] { keyChar }, keyState);
            }
        } else {
            final char x = this.keyCharSet[fxKeycode];
            this.keyStateSet.clear(fxKeycode);
            if (context.hasFocus()) {
                context.getSceneInterface().keyEvent(AbstractEvents.KEYEVENT_RELEASED, fxKeycode, new char[] { x }, keyState);
            }
        }

    }

    public int retrieveKeyState() {
        int embedModifiers = 0;

        if (keyStateSet.get(KeyEvent.VK_SHIFT)) {
            embedModifiers |= AbstractEvents.MODIFIER_SHIFT;
        }

        if (keyStateSet.get(KeyEvent.VK_CONTROL)) {
            embedModifiers |= AbstractEvents.MODIFIER_CONTROL;
        }

        if (keyStateSet.get(KeyEvent.VK_ALT)) {
            embedModifiers |= AbstractEvents.MODIFIER_ALT;
        }

        if (keyStateSet.get(KeyEvent.VK_META)) {
            embedModifiers |= AbstractEvents.MODIFIER_META;
        }
        return embedModifiers;
    }

    protected boolean isCovered(final int x, final int y) {
        if (x < 0 || x >= context.getStage().getWidth()) {
            return false;
        }
        if (y < 0 || y >= context.getStage().getHeight()) {
            return false;
        }
        final ByteBuffer data = context.getImage().getData(0);
        final int alpha = Byte.toUnsignedInt(data.get(this.alphaByteOffset + 4 * (y * context.getStage().getWidth() + x)));
        return alpha > alphaByteOffset;
    }

}
