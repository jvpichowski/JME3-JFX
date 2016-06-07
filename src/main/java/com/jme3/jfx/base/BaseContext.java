package com.jme3.jfx.base;

import com.jme3.app.Application;
import com.jme3.jfx.JFxManager;
import com.jme3.jfx.Layer;

import java.lang.ref.PhantomReference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation
 *
 * Created by jan on 01.06.16.
 */
abstract class BaseContext implements Context {

    private Application application;
    private JFxManager jFxManager;
    private String name = "";
    private boolean created = false;

    //LinkedList is faster for rearranging elements (pushFront/Back)
    private List<Layer> layers = new LinkedList<>();

    /**
     * Really needed?
     *
     * @param name
     * @return
     */
    @Override
    public final void setName(String name) {
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public boolean isCreated() {
        return created;
    }

    /**
     * FOR INTERNAL USE ONLY
     * <p>
     * is called at jfxManager.createLayer
     *
     * @param jfxManager
     */
    @Override
    public void create(JFxManager jfxManager) {
        if(isCreated()){
            throw new IllegalStateException("Context is already created");
        }
        this.jFxManager = jfxManager;
        this.application = jfxManager.getApplication();
        created = true;
    }

    @Override
    public void restart() {

    }

    /**
     * Destroy this fxcontext
     */
    @Override
    public void destroy() {
        created = false;
    }

    /**
     * @return the jME3 Application
     */
    @Override
    public final Application getApplication() {
        return application;
    }

    /**
     * @return the manger which created this fxcontext
     */
    @Override
    public final JFxManager getJFxManager() {
        return jFxManager;
    }


    @Override
    public List<Layer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    protected void addLayer(Layer layer){
        layers.add(layer);
        reorderLayers();
    }

    protected void removeLayer(Layer layer){
        layers.remove(layer);
        reorderLayers();
    }

    protected abstract void reorderLayers();

    public void pushFront(Layer layer){
        if(layers.size() <= 1){
            return;
        }
        layers.remove(layer);
        layers.add(layer);
        reorderLayers();
    }

    public void pushBack(Layer layer){
        if(layers.size() <= 1){
            return;
        }
        layers.remove(layer);
        layers.add(0, layer);
        reorderLayers();
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
        destroy();
        super.finalize();
    }
}
