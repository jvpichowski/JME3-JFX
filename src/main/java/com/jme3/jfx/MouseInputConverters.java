package com.jme3.jfx;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.TempVars;

import java.awt.*;
import java.util.function.BiFunction;

/**
 * Created by jan on 07.06.16.
 */
public final class MouseInputConverters {

    /**
     * Discards every mouse input. To discard key input call loseFocus on the context.
     */
    public static final BiFunction<Context, Point, Point> Discard = (c,p) -> null;

    /**
     *
     */
    public static final BiFunction<Context, Point, Point> FullscreenInput = (c,p) -> new Point(p.x, c.getHeight()-p.y);


    public static final BiFunction<Context, Point, Point> FullscreenCrosshairsToGeometry(
            Camera camera, Geometry geometry, int width, int height, Node collisionTree){
        return (c,p) -> {
            Ray ray = new Ray(camera.getLocation(), camera.getDirection());

            return collide(ray, geometry, width, height, collisionTree);
        };
    }

    /**
     *
     * @param camera to which scene the geometry is added
     * @param geometry
     * @param width of the texture
     * @param height of the texture
     * @param collisionTree null if you check only this geometry or a Node with all Spatials attached,
     *                      that could be between camera and geometry.
     * @return
     */
    public static final BiFunction<Context, Point, Point> FullscreenCursorToGeometry(
            Camera camera, Geometry geometry, int width, int height, Node collisionTree){
        return (c,p) -> {
            Vector3f click3d = camera.getWorldCoordinates(new Vector2f(p.x, p.y), 0f).clone();
            Vector3f dir = camera.getWorldCoordinates(new Vector2f(p.x, p.y), 1f).subtractLocal(click3d).normalizeLocal();
            Ray ray = new Ray(click3d, dir);

            return collide(ray, geometry, width, height, collisionTree);
        };
    }

    private static final Point collide(Ray ray, Geometry geometry, int width, int height, Node collisionTree){
        CollisionResults results = new CollisionResults();

        if(collisionTree != null){
            collisionTree.collideWith(ray, results);
        }else{
            geometry.collideWith(ray, results);
        }

        CollisionResult collision = results.getClosestCollision();
        if (collision == null) {
            return null;
        }
        Geometry geom = collision.getGeometry();
        if (geom != geometry) {
            return null;
        }
        int ti = collision.getTriangleIndex();
        TempVars tmp = TempVars.get();

        Vector3f p0 = tmp.vect1;
        Vector3f p1 = tmp.vect2;
        Vector3f p2 = tmp.vect3;
        Vector2f t0 = tmp.vect2d;
        Vector2f t1 = tmp.vect2d2;
        Vector2f t2 = new Vector2f();


        Utilities.getTriangle(collision.getGeometry().getMesh(), VertexBuffer.Type.Position, ti, p0, p1, p2);
        Utilities.getTriangle(collision.getGeometry().getMesh(), VertexBuffer.Type.TexCoord, ti, t0, t1, t2);

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

        tmp.release();

        float x = width * s + 0.5f;
        float y = height * (1 - t) + 0.5f;

        return new Point(Math.round(x), Math.round(y));
    }
}
