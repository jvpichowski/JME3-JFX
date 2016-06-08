package com.jme3.jfx;

import com.jme3.app.AppTask;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.jfx.base.Context;
import com.jme3.jfx.fxcontext.FxContext;
import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import jdk.nashorn.internal.ir.Block;
import org.lwjgl.opengl.Display;

import javax.swing.*;
import java.util.concurrent.*;
import java.util.function.Consumer;


/**
 * The entrance for every FxApplication. The Java Fx Thread is handled
 * by this AppState. It is recommend to launch FxApplication through
 * the methods provided by this AppState.
 *
 * Created by jan on 11.05.16.
 */
public final class JFxManager extends BaseAppState {

    private int insetsTop = 0;

    private final InputAdapter inputAdapter = new InputAdapter();
    private boolean exitOnCleanup = true;
    //those are called on the jME3 thread
    private BlockingQueue<Runnable> initTasks = new LinkedBlockingQueue<>();
    private BlockingQueue<Runnable> cleanTasks = new LinkedBlockingQueue<>();
    private CopyOnWriteArrayList<Consumer<Float>> updateTasks = new CopyOnWriteArrayList<>();

    public Layer launch(Context context, FxApplication application){
        context.create(this);
        return context.createLayer(application);
    }

    public InputAdapter getInputAdapter(){
        return inputAdapter;
    }

    @Deprecated
    public Stage launch(FxContext context, FxApplication application){
        context.create(this);
        Stage stage = context.getStage();
        try {
            application.start(new Stage.Layer(stage));
        }catch (Exception e) {
            context.destroy();
            e.printStackTrace();
            return null;
        }
        return stage;
//        return enqueue(new Callable<Stage>() {
//            @Override
//            public Stage call() throws Exception {
//                fxcontext.create(JFxManager.this);
//                Stage stage = fxcontext.getStage();
//                try {
//                    application.start(stage);
//                }catch (Exception e) {
//                    fxcontext.destroy();
//                    e.printStackTrace();
//                    return null;
//                }
//                return stage;
//            }
//        });
    }

    //move to stage because of stageposition
    public double getScreenY(double stageY){
        try {
            return Display.getX() /*+ insetsTop*/ + stageY;
        }catch (Exception ex){
            throw new IllegalStateException("This jME3 Context doesn't support this operation! You have to use lgjwl");
        }
    }

    /**
     * On calling this the RawInputListener is added to the InputManager
     */
    public void beginInput(){
        inputAdapter.create(getApplication());
    }

    /**
     * On calling this the RawInputListener is removed from the InputManager
     */
    public void endInput(){
        inputAdapter.destroy();
    }

    public void addOnUpdate(Consumer<Float> onUpdate){
        updateTasks.add(onUpdate);
    }

    public void removeOnUpdate(Consumer<Float> onUpdate){
        updateTasks.remove(onUpdate);
    }

    public void onInit(Runnable r){
        enqueue(() -> getApplication().enqueue(r));
    }

    public void onClean(Runnable r) {
        cleanTasks.add(r);
    }


    /**
     * Called during initialization once the application state is
     * attached and before onEnable() is called.
     *
     * @param app the application
     */
    @Override
    protected void initialize(Application app) {
        //obtain insets
        JFrame f = new JFrame();
        f.setLayout(null);
        f.pack();
        insetsTop = f.getInsets().top;
        f.dispose();

        System.out.println("INIT JFxManager");
        PlatformImpl.startup(() -> {});
        boolean useRecommendedJFXSettings = false;
        if (useRecommendedJFXSettings) {

            System.setProperty("javafx.animation.fullspeed", "true"); // reduce laggyness of animations, bad for business apps great for games
            System.setProperty("prism.order", "sw"); // use software rendering, keep the gpu free for jme, use another core for jfx in software mode and all win
            System.setProperty("prism.vsync", "false"); // jme should limit rendering speed anyway or?
            System.setProperty("sun.java2d.xrender", "f");// workaround for linux specific bug
        }
        initTasks.forEach(Runnable::run);
    }


    /**
     * If exitOnClean the Java Fx Platform will be destroyed when
     * this AppState is cleaned.
     *
     * @param exitOnCleanup default: true
     */
    public void exitOnCleanup(boolean exitOnCleanup){
        this.exitOnCleanup = exitOnCleanup;
    }

    /**
     * Called after the application state is detached or during
     * application shutdown if the state is still attached.
     * onDisable() is called before this cleanup() method if
     * the state is enabled at the time of cleanup.
     *
     * @param app the application
     */
    @Override
    protected void cleanup(Application app) {
        cleanTasks.forEach(Runnable::run);
        if(exitOnCleanup) {
            Platform.exit();
        }

    }


    @Override
    public void update(float tpf) {
        super.update(tpf);
        updateTasks.forEach(t -> t.accept(tpf));
    }

    /**
     * Called when the state is fully enabled, ie: is attached
     * and isEnabled() is true or when the setEnabled() status
     * changes after the state is attached.
     */
    @Override
    protected void onEnable() {

    }

    /**
     * Called when the state was previously enabled but is
     * now disabled either because setEnabled(false) was called
     * or the state is being cleaned up.
     */
    @Override
    protected void onDisable() {

    }

    public void enqueue(Runnable task) {
        if(!isInitialized()){
            System.out.println("JFxManger is not initialized yet!");
            initTasks.add(() -> enqueue(task));
            return;
        }
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }

    public <V> Future<V> enqueue(Callable<V> callable){
        AppTask<V> task = new AppTask<>(callable);
        if(!isInitialized()){
            System.out.println("JFxManger is not initialized yet!");
            initTasks.add(() -> enqueue(() -> {
                if (Platform.isFxApplicationThread()){
                    task.invoke();
                }else{
                    Platform.runLater(() -> {
                        if(!task.isCancelled()) {
                            task.invoke();
                        }
                    });
                }
            }));
            return task;
        }
        if (Platform.isFxApplicationThread()){
            task.invoke();
        }else{
            Platform.runLater(() -> {
                if(!task.isCancelled()) {
                    task.invoke();
                }
            });
        }
        return task;
    }


}
