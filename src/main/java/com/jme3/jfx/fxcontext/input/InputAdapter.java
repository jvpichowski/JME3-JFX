package com.jme3.jfx.fxcontext.input;

import com.jme3.collision.CollisionResult;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.jfx.fxcontext.FxContext;
import com.jme3.jfx.Stage;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.TempVars;
import com.jme3.jfx.utils.VertexUtils;

import java.util.function.Consumer;

/**
 * Created by jan on 13.05.16.
 */
public interface InputAdapter {

    static AlphaInputAdapter createAlphaAdapter(){
        return new AlphaInputAdapter();
    }

    static AlphaInputAdapter createAlphaAdapter(int alphaLimit){
        return new AlphaInputAdapter(alphaLimit);
    }

    static InputAdapter createEatupAdapter(){
        return new EatupInputAdapter();
    }

    static MouseButtonEvent translate(Stage stage, int buttonIndex, boolean isPressed, long time, float x, float y){
        MouseButtonEvent nevt = new MouseButtonEvent(buttonIndex, isPressed, (int) x, (int) y);
        nevt.setTime(time);

        return nevt;
    }

    static MouseButtonEvent translate(Stage stage, int buttonIndex, boolean isPressed, long time, CollisionResult collision) {
        int ti = collision.getTriangleIndex();
        TempVars tmp = TempVars.get();

        Vector3f p0 = tmp.vect1;
        Vector3f p1 = tmp.vect2;
        Vector3f p2 = tmp.vect3;
        Vector2f t0 = tmp.vect2d;
        Vector2f t1 = tmp.vect2d2;
        Vector2f t2 = new Vector2f();


        VertexUtils.getTriangle(collision.getGeometry().getMesh(), VertexBuffer.Type.Position, ti, p0, p1, p2);
        VertexUtils.getTriangle(collision.getGeometry().getMesh(), VertexBuffer.Type.TexCoord, ti, t0, t1, t2);

        Vector3f cp = collision.getContactPoint();
        collision.getGeometry().worldToLocal(cp, cp);

        Vector3f vn = p2.subtract(p1, tmp.vect4).crossLocal(p1.subtract(p0, tmp.vect5));
        float A = vn.length();
        Vector3f n = tmp.vect6.set(vn).divideLocal(A);
        float u = FastMath.abs((p2.subtract(p1, tmp.vect7).crossLocal(cp.subtract(p1, tmp.vect8))).dot(n) / A);
        float v = FastMath.abs((p0.subtract(p2, tmp.vect7).crossLocal(cp.subtract(p2, tmp.vect8))).dot(n) / A);
        float w = 1 - u - v;

        float s = t0.x * u + t1.x * v + t2.x * w;
        float t = t0.y * u + t1.y * v + t2.y * w;

        float x = stage.getWidth() * s + 0.5f;
        float y = stage.getHeight() * (1 - t) + 0.5f;

        MouseButtonEvent nevt = new MouseButtonEvent(buttonIndex, isPressed, (int) x, (int) y);
        nevt.setTime(time);

        tmp.release();

        return nevt;

    }

    /**
     * is called on creation of fxcontext or if already created instant
     * @param context
     */
    void setContext(FxContext context);

    void onMouseMotionEvent(final MouseMotionEvent evt, Consumer<MouseMotionEvent> callAfter);

    void onMouseButtonEvent(final MouseButtonEvent evt, Consumer<MouseButtonEvent> callAfter);

    void onKeyEvent(final KeyInputEvent evt, Consumer<KeyInputEvent> callAfter);

}
