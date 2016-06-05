package com.jme3.jfx;

import com.jme3.app.AppTask;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.awt.AwtKeyInput;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.jfx.base.Context;
import com.jme3.jfx.fxcontext.FxContext;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.embed.AbstractEvents;
import javafx.application.Platform;
import org.lwjgl.opengl.Display;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
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

    private InputAdapter inputAdapter = new InputAdapter();
    private boolean exitOnCleanup = true;
    //those are called on the jME3 thread
    private BlockingQueue<Runnable> initTasks = new LinkedBlockingQueue<>();
    private BlockingQueue<Runnable> cleanTasks = new LinkedBlockingQueue<>();

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
     *
     * @param exitOnCleanup
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
        boolean applyMouseInput(int eventType ,int button, int wheelRotation, int jME_x, int jME_y);

        boolean applyKeyInput(int eventType, int fx_keycode);

    }

    public final class InputAdapter{

        //should ne used on fx thread
        private BitSet keyStateSet = new BitSet(0xFF);
        private char[] keyCharSet = new char[Short.MAX_VALUE * 3];
        boolean[] mouseButtonState = new boolean[3];


        private List<InputListener> listeners = new LinkedList<>();

        private InputAdapter(){

        }

        public int getEmbeddedModifiers(){
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

        public boolean getKeyState(int key){
            return keyStateSet.get(key);
        }

        public char getKeyChar(int fx_keycode){
            return keyCharSet[fx_keycode];
        }

        public boolean getMouseButtonState(int button){
            if(button < 0 || button >= 3){
                throw new IllegalArgumentException("Button has to be [0,1,2]");
            }
            return mouseButtonState[button];
        }

        public void register(InputListener listener){
            listeners.add(listener);
        }

        public void unregister(InputListener listener){
            listeners.remove(listener);
        }

        public void apply(final MouseMotionEvent evt, Consumer<MouseMotionEvent> callAfter){
            int type = AbstractEvents.MOUSEEVENT_MOVED;
            int button = AbstractEvents.MOUSEEVENT_NONE_BUTTON;

            final int wheelRotation = (int) Math.round(evt.getDeltaWheel() / -120.0);

            if (wheelRotation != 0) {
                type = AbstractEvents.MOUSEEVENT_WHEEL;
                button = AbstractEvents.MOUSEEVENT_NONE_BUTTON;
            } else if (getInputAdapter().getMouseButtonState(0)) {
                button = AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
            } else if (getInputAdapter().getMouseButtonState(1)) {
                button = AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
            } else if (getInputAdapter().getMouseButtonState(2)) {
                button = AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
            }

            final int ftype = type;
            final int fbutton = button;
            listeners.forEach(l -> l.applyMouseInput(ftype, fbutton, wheelRotation, evt.getX(), evt.getY()));
        }

        public void apply(final MouseButtonEvent evt, Consumer<MouseButtonEvent> callAfter){
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

        public void apply(final KeyInputEvent evt, Consumer<KeyInputEvent> callAfter){
            final char keyChar = evt.getKeyChar();

            int fx_keycode = AwtKeyInput.convertJmeCode(evt.getKeyCode());
            if (fx_keycode > keyCharSet.length) {
                switch (keyChar) {
                    case '\\':
                        fx_keycode = java.awt.event.KeyEvent.VK_BACK_SLASH;
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

    }
}
