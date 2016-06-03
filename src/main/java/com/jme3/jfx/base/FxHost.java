package com.jme3.jfx.base;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.embed.AbstractEvents;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implementation
 *
 * Created by jan on 30.05.16.
 */
final class FxHost implements HostInterface {

    public interface FxContainerBridge{
        void setStagePeer(EmbeddedStageInterface embeddedStage);
        EmbeddedStageInterface getStagePeer();
        int getWidth();
        int getHeight();
        EmbeddedSceneInterface getSceneInterface();
        void setSceneInterface(EmbeddedSceneInterface embeddedScene);
        void repaint();
    }

    private FxContainerBridge bridge;

    public FxHost(FxContainerBridge bridge){
        if(bridge == null){
            throw new IllegalArgumentException("Null is not allowed");
        }
        this.bridge = bridge;
    }

    @Override
    public void setEmbeddedStage(final EmbeddedStageInterface embeddedStage) {
        bridge.setStagePeer(embeddedStage);
        if (bridge.getStagePeer() == null) {
            return;
        }
        if (bridge.getWidth() > 0 && bridge.getHeight() > 0) {
            bridge.getStagePeer().setSize(bridge.getWidth(), bridge.getHeight());
        }

        bridge.getStagePeer().setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
    }

    @Override
    public void setEmbeddedScene(final EmbeddedSceneInterface embeddedScene) {
        bridge.setSceneInterface(embeddedScene);
        if (bridge.getSceneInterface() == null) {
            return;
        }

        // 8_u60 and later fix
        try {
            final Method scaler = embeddedScene.getClass().getMethod("setPixelScaleFactor", float.class);
            scaler.setAccessible(true);
            scaler.invoke(embeddedScene, 1f);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        if (bridge.getWidth() > 0 && bridge.getHeight() > 0) {
            bridge.getSceneInterface().setSize(bridge.getWidth(), bridge.getHeight());
        }

    }

    @Override
    public boolean requestFocus() {
        return true;
    }

    @Override
    public boolean traverseFocusOut(final boolean forward) {
        System.out.println("Called traverseFocusOut("+forward+")");
        return true;
    }

    @Override
    public void setPreferredSize(final int width, final int height) {
    }


    @Override
    public void repaint() {
        bridge.repaint();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        //this.fxcontext.setFxEnabled(enabled);
    }

    @Override
    public void setCursor(final CursorFrame cursorFrame) {
//		if (this.fxcontext.cursorDisplayProvider != null) {
//			this.fxcontext.cursorDisplayProvider.showCursor(cursorFrame);
//		}
    }

    /**
     * Grabs focus on this window.
     * <p>
     * All mouse clicks that occur in this window's client area or client-areas
     * of any of its unfocusable owned windows are delivered as usual. Whenever
     * a click occurs on another app's window (not related via the ownership
     * relation with this one, or a focusable owned window), or on non-client
     * area of any window (titlebar, etc.), or any third-party app's window, or
     * native OS GUI (e.g. a taskbar), the grab is automatically reset, and the
     * window that held the grab receives the FOCUS_UNGRAB event.
     * <p>
     * Note that for this functionality to work correctly, the window must have
     * a focus upon calling this method. All owned popup windows that should be
     * operable during the grabbed focus state (e.g. nested popup menus) must
     * be unfocusable (see {@link #setFocusable}). Clicking a focusable owned
     * window will reset the grab due to a focus transfer.
     * <p>
     * The click that occurs in another window and causes resetting of the grab
     * may or may not be delivered to that other window depending on the native
     * OS behavior.
     * <p>
     * If any of the application's windows already holds the grab, it is reset
     * prior to grabbing the focus for this window. The method may be called
     * multiple times for one window. Subsequent calls do not affect the grab
     * status unless it is reset between the calls, in which case the focus
     * is grabbed again.
     * <p>
     * Note that grabbing the focus on an application window may prevent
     * delivering certain events to other applications until the grab is reset.
     * Therefore, if the application has finished showing popup windows based
     * on a user action (e.g. clicking a menu item), and doesn't require the
     * grab any more, it should call the {@link #ungrabFocus} method. The
     * FOCUS_UNGRAB event signals that the grab has been reset.
     * <p>
     * A user event handler associated with a menu item must be invoked after
     * resetting the grab. Otherwise, if a developer debugs the application and
     * has installed a breakpoint in the event handler, the debugger may become
     * unoperable due to events blocking for other applications on some
     * platforms.
     *
     * @return {@code true} if the operation is successful
     * @throws IllegalStateException if the window isn't focused currently
     */
    @Override
    public boolean grabFocus() {
        return true;
    }

    /**
     * Manually ungrabs focus grabbed on this window previously.
     * <p>
     * This method resets the grab, and forces sending of the FOCUS_UNGRAB
     * event. It should be used when popup windows (such as menus) should be
     * dismissed manually, e.g. when a user clicks a menu item which usually
     * causes the menus to hide.
     *
     * @see #grabFocus
     */
    @Override
    public void ungrabFocus() {
    }
}