package com.jme3.jfx;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.function.BiPredicate;

/**
 * Contains some default InputConsumerModes.
 */
public final class InputConsumerModes {

    /**
     * Don't listen to any mouse input.
     */
    public static final BiPredicate<Layer, Point> Discard = (l,p) -> false;

    /**
     * Listen to mouse input if cursor is not pointing at a transparent area.
     */
    public static final BiPredicate<Layer, Point> StrictAlphaBased = AlphaBased(0);

    /**
     * Listen to mouse input if cursor is not pointing at a transparent area.
     * @param threshold [0-255] when an area is transparent
     * @return
     */
    public static final BiPredicate<Layer, Point> AlphaBased(final int threshold){
        return (l,p) -> {
            if(!AllInArea.test(l,p)){
                return false;
            }
            final ByteBuffer data = ((BaseLayer)l).getFxContainer().getImage().getData(0);
            final int alpha = Byte.toUnsignedInt(data.get(((BaseLayer)l).getFxContainer().getAlphaByteOffset() + 4 * (p.y * l.getWidth() + p.x)));
            return alpha > threshold;
        };
    }

    /**
     * Listen to all mouse inputs.
     */
    public static final BiPredicate<Layer, Point> All = (l,p) -> true;

    /**
     * Listen only to mouse input if the cursor is pointing at this Layer.
     */
    public static final BiPredicate<Layer, Point> AllInArea = (l,p) -> {
        if (p.x < 0 || p.x >= l.getWidth()) {
            return false;
        }
        if (p.y < 0 || p.x >= l.getHeight()) {
            return false;
        }
        return true;
    };

}
