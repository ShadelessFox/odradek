package sh.adelessfox.odradek.viewer.model.viewport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is responsible for triggering the {@link Viewport#render()} function
 * periodically according to the current display refresh rate.
 * <p>
 * When the viewport is not visible, the rendering is paused to reduce CPU and GPU usage.
 */
final class ViewportAnimator {
    private static final Logger log = LoggerFactory.getLogger(ViewportAnimator.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final Lock renderLock = new ReentrantLock();
    private final Condition canRender = renderLock.newCondition();

    private final Viewport viewport;
    private final EventHandler handler = new EventHandler();

    public ViewportAnimator(Viewport viewport) {
        this.viewport = viewport;
    }

    public void start() {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("Animator is already running");
        }

        viewport.addHierarchyListener(handler);
        viewport.addComponentListener(handler);
        executorService.submit(this::loop);
    }

    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            throw new IllegalStateException("Animator is not running");
        }

        viewport.removeHierarchyListener(handler);
        viewport.removeComponentListener(handler);
    }

    private void loop() {
        while (isRunning.get()) {
            renderLock.lock();
            try {
                while (isPaused.get()) {
                    canRender.awaitUninterruptibly();
                }
            } finally {
                renderLock.unlock();
            }
            try {
                SwingUtilities.invokeAndWait(() -> {
                    if (viewport.isValid()) {
                        viewport.render();
                    }
                });
            } catch (InterruptedException ignored) {
            } catch (InvocationTargetException e) {
                log.error("Error during rendering", e.getTargetException());
                stop();
            }
        }
    }

    private void updatePause() {
        renderLock.lock();

        try {
            isPaused.set(shouldPause());
            canRender.signal();
        } finally {
            renderLock.unlock();
        }
    }

    private boolean shouldPause() {
        return viewport.getWidth() <= 0 || viewport.getHeight() <= 0 || !viewport.isShowing();
    }

    private final class EventHandler implements HierarchyListener, ComponentListener {
        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED && (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                updatePause();
            }
        }

        @Override
        public void componentResized(ComponentEvent e) {
            updatePause();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            updatePause();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            updatePause();
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            updatePause();
        }
    }
}
