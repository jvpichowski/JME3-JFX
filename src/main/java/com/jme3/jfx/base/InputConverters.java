package com.jme3.jfx.base;

import java.awt.*;
import java.util.function.BiFunction;

/**
 * Created by jan on 07.06.16.
 */
public final class InputConverters {

    /**
     * Discards every mouse input. To discard key input call loseFocus on the context.
     */
    public static final BiFunction<Context, Point, Point> Discard = (c,p) -> null;

    /**
     *
     */
    public static final BiFunction<Context, Point, Point> FullscreenInput = (c,p) -> new Point(p.x, c.getHeight()-p.y);

//    public static final BiFunction<Context, Point, Point> Quad = null;

}
