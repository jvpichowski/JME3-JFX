package com.jme3.jfx.example;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.base.Configuration;
import com.jme3.jfx.base.Context;
import com.jme3.jfx.base.InputConsumerModes;
import com.jme3.jfx.base.RenderSystem;
import com.jme3.jfx.utils.VertexUtils;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.util.TempVars;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

import java.awt.*;

/**
 * Created by jan on 08.06.16.
 */
public class FxInWorldSpace {

    public static void main(String[] args){
        //create the jME3 app
        JFxManager jFxManager = new JFxManager();
        SimpleApplication app = new SimpleApplication(jFxManager, new FlyCamAppState()) {
            @Override
            public void simpleInitApp() {
                Box b = new Box(1, 1, 1);                                   // renderToFullscreen cube shape
                Geometry geom = new Geometry("Box", b);                     // renderToFullscreen cube geometry from the shape
                Material mat = new Material(getAssetManager(),
                        "Common/MatDefs/Misc/Unshaded.j3md");               // renderToFullscreen a simple material
                mat.setColor("Color", ColorRGBA.Blue);                      // set color of material to blue
                geom.setMaterial(mat);                                      // set the cube's material
                getRootNode().attachChild(geom);                            // make the cube appear in the scene
                //move cube behind fx quad
                geom.move(0,0,-10);
            }
        };
        //wait until the JFxManager is initialized until we attach our scene
        jFxManager.onInit(() -> {
            app.getStateManager().getState(FlyCamAppState.class).setEnabled(true);
            app.getInputManager().setCursorVisible(true);
            //enable input
            jFxManager.beginInput();

            Configuration config = new Configuration();
            config.setSingleLayer(false);
            config.setStaticLayers(true);

            //create the geometry to which the fx context should be rendered
            Geometry geometry = new Geometry("fx geometry", new Quad(2, 2, false));
            app.getRootNode().attachChild(geometry);

            //create a render target
            final int width = 100;
            final int height = 100;
            config.setRenderSystem(RenderSystem.renderToGeometry(width, height, geometry));

            //create a custom input converter
            //this converter casts a ray from the 2d click to the geometry
            config.setInputConverter((context, point) -> {

                CollisionResults results = new CollisionResults();

                Vector3f click3d = app.getCamera().getWorldCoordinates(new Vector2f(point.x, point.y), 0f).clone();
                Vector3f dir = app.getCamera().getWorldCoordinates(new Vector2f(point.x, point.y), 1f).subtractLocal(click3d).normalizeLocal();
                Ray ray = new Ray(click3d, dir);
                app.getRootNode().collideWith(ray, results);

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

                tmp.release();

                float x = width * s + 0.5f;
                float y = height * (1 - t) + 0.5f;

                return new Point(Math.round(x), Math.round(y));
            });

            //create the context
            Context context = Context.create(config);

            jFxManager.launch(context, layer -> {
                //create the scene
                Group root = new Group();
                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);

                //add a button
                javafx.scene.control.Button b = new javafx.scene.control.Button("show frame");
                b.setOnMouseClicked(event -> {
                    if(scene.getFill().equals(Color.TRANSPARENT)) {
                        b.setText("remove frame");
                        scene.setFill(Color.RED);
                    }else{
                        b.setText("show frame");
                        scene.setFill(Color.TRANSPARENT);
                    }

                });
                root.getChildren().add(b);

                //listen to every input which is forwarded to the context
                layer.setInputConsumerMode(InputConsumerModes.AllInArea);
                //show the scene
                layer.setScene(scene);
                layer.show();
            });
        });

        //don't forget to start jME3
        app.start();
    }

}
