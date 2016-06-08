package com.jme3.jfx.base;

import com.jme3.jfx.Layer;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.function.BiPredicate;

/**
 * Created by jan on 08.06.16.
 */
public final class InputConsumerModes {

    public static final BiPredicate<Layer, Point> Discard = (l,p) -> false;

    public static final BiPredicate<Layer, Point> StrictAlphaBased = AlphaBased(0);

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

    public static final BiPredicate<Layer, Point> All = (l,p) -> true;

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
