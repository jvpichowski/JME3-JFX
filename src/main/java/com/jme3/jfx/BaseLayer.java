package com.jme3.jfx;

import com.sun.javafx.embed.AbstractEvents;
import javafx.scene.Scene;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.ref.PhantomReference;
import java.lang.ref.WeakReference;
import java.util.function.BiPredicate;

/**
 * Created by jan on 05.06.16.
 */
abstract class BaseLayer implements Layer{

    private final FxContainer fxContainer;
    private final BaseContext context;
    private BiPredicate<Layer, Point> inputConsumerMode = InputConsumerModes.AllInArea;

    private boolean hasFocus = false;

    private float x = 0;
    private float y = 0;

    public BaseLayer(BaseContext context, FxContainer fxContainer){
        this.context = context;
        this.fxContainer = fxContainer;
    }

    protected BaseContext getContext(){
        return context;
    }

    FxContainer getFxContainer(){
        return fxContainer;
    }


    /**
     * Could be called from any thread.
     */
    @Override
    public void toFront() {
        context.getApplication().enqueue(() -> context.pushFront(this));
    }

    /**
     * Could be called from any thread.
     */
    @Override
    public void toBack() {
        context.getApplication().enqueue(() -> context.pushBack(this));
    }

    @Override
    public void setScene(Scene scene){
        fxContainer.setScene(scene);
    }

    @Override
    public void loseFocus() {
        hasFocus = false;
    }

    @Override
    public void grabFocus() {
        hasFocus = true;
    }

    @Override
    public boolean hasFocus() {
        return hasFocus;
    }

    @Override
    public void setInputConsumerMode(BiPredicate<Layer, Point> mode) {
        this.inputConsumerMode = mode;
    }

    @Override
    public void close() {
        fxContainer.destroy();
    }

    @Override
    public int getWidth() {
        return fxContainer.getWidth();
    }

    @Override
    public int getHeight() {
        return fxContainer.getHeight();
    }

    protected final void setX(float x){
        this.x = x;
    }

    protected final void setY(float y){
        this.y = y;
    }

    @Override
    public final float getX() {
        return x;
    }

    @Override
    public final float getY() {
        return y;
    }

    @Override
    public final void setTitle(String title) {
        fxContainer.setName(title);
    }


    /**
     * Convert the x-coordinate from context space to layer space
     *
     * @param context_x
     * @return
     */
    protected final float contextToLayerX(float context_x){
        return context_x-getX();
    }

    /**
     * Convert the y-coordinate from context space to layer space
     *
     * @param context_y
     * @return
     */
    protected final float contextToLayerY(float context_y){
        return context_y-getY();
    }


    protected boolean applyMouseInput(JFxManager jFxManager, int eventType, int button, int wheelRotation, int x, int y, int xAbs, int yAbs){
        if(!getFxContainer().isActive()){
            return false;
        }
        //if not covered return false here - release focus?
        if(!inputConsumerMode.test(BaseLayer.this, new Point(x,y))){
            loseFocus();
            return false;
        }

        if(eventType == AbstractEvents.MOUSEEVENT_PRESSED){
            grabFocus();
        }
        if(eventType == AbstractEvents.MOUSEEVENT_RELEASED){
            grabFocus();
        }

        getFxContainer().mouseEvent(eventType, button,
                jFxManager.getInputAdapter().getMouseButtonState(0),
                jFxManager.getInputAdapter().getMouseButtonState(1),
                jFxManager.getInputAdapter().getMouseButtonState(2),
                x, y, xAbs, yAbs,
                jFxManager.getInputAdapter().getKeyState(KeyEvent.VK_SHIFT),
                jFxManager.getInputAdapter().getKeyState(KeyEvent.VK_CONTROL),
                jFxManager.getInputAdapter().getKeyState(KeyEvent.VK_ALT),
                jFxManager.getInputAdapter().getKeyState(KeyEvent.VK_META), wheelRotation,
                button == AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON);

        return true;
    }

    protected boolean applyKeyInput(JFxManager jFxManager, int eventType, int fx_keycode) {
        if(!getFxContainer().isActive()){
            return false;
        }
        if(!hasFocus()){
            return false;
        }
        getFxContainer().keyEvent(eventType, fx_keycode,
                new char[] { jFxManager.getInputAdapter().getKeyChar(fx_keycode) },
                jFxManager.getInputAdapter().getEmbeddedModifiers());
        return true;
    }

    /**
     * Called by the garbage collector on an object when garbage collection
     * determines that there are no more references to the object.
     * A subclass overrides the {@code finalize} method to dispose of
     * system resources or to perform other cleanup.
     * <p>
     * The general contract of {@code finalize} is that it is invoked
     * if and when the Java&trade; virtual
     * machine has determined that there is no longer any
     * means by which this object can be accessed by any thread that has
     * not yet died, except as a result of an action taken by the
     * finalization of some other object or class which is ready to be
     * finalized. The {@code finalize} method may take any action, including
     * making this object available again to other threads; the usual purpose
     * of {@code finalize}, however, is to perform cleanup actions before
     * the object is irrevocably discarded. For example, the finalize method
     * for an object that represents an input/output connection might perform
     * explicit I/O transactions to break the connection before the object is
     * permanently discarded.
     * <p>
     * The {@code finalize} method of class {@code Object} performs no
     * special action; it simply returns normally. Subclasses of
     * {@code Object} may override this definition.
     * <p>
     * The Java programming language does not guarantee which thread will
     * invoke the {@code finalize} method for any given object. It is
     * guaranteed, however, that the thread that invokes finalize will not
     * be holding any user-visible synchronization locks when finalize is
     * invoked. If an uncaught exception is thrown by the finalize method,
     * the exception is ignored and finalization of that object terminates.
     * <p>
     * After the {@code finalize} method has been invoked for an object, no
     * further action is taken until the Java virtual machine has again
     * determined that there is no longer any means by which this object can
     * be accessed by any thread that has not yet died, including possible
     * actions by other objects or classes which are ready to be finalized,
     * at which point the object may be discarded.
     * <p>
     * The {@code finalize} method is never invoked more than once by a Java
     * virtual machine for any given object.
     * <p>
     * Any exception thrown by the {@code finalize} method causes
     * the finalization of this object to be halted, but is otherwise
     * ignored.
     *
     * @throws Throwable the {@code Exception} raised by this method
     * @jls 12.6 Finalization of Class Instances
     * @see WeakReference
     * @see PhantomReference
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
