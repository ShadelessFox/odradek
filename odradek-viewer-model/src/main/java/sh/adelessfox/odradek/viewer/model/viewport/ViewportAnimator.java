package sh.adelessfox.odradek.viewer.model.viewport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * This class is responsible for triggering the supplied {@code requestRender} callback
 * periodically according to the {@link #setRefreshRate(int) specified refresh rate}.
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

    private final JComponent viewport;
    private final EventHandler handler = new EventHandler();
    private final Runnable requestRender;

    private int refreshRate = -1;
    private long lastRenderTime = 0;

    public ViewportAnimator(JComponent viewport, Runnable requestRender) {
        this.viewport = viewport;
        this.requestRender = requestRender;
    }

    public void start() {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("Animator is already running");
        }

        viewport.addHierarchyListener(handler);
        viewport.addComponentListener(handler);
        viewport.addFocusListener(handler);
        executorService.submit(this::loop);
    }

    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            throw new IllegalStateException("Animator is not running");
        }

        viewport.removeHierarchyListener(handler);
        viewport.removeComponentListener(handler);
        viewport.removeFocusListener(handler);

        renderLock.lock();
        try {
            canRender.signal();
        } finally {
            renderLock.unlock();
        }
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    private void loop() {
        while (isRunning.get()) {
            waitUntilCanRender();
            if (isRunning.get()) {
                render();
            }
        }
    }

    private void render() {
        var lock = new Object();
        var dispatched = new boolean[1];
        SwingUtilities.invokeLater(() -> {
            requestRender.run();
            dispatched[0] = true;
            synchronized (lock) {
                lock.notify();
            }
        });
        if (!dispatched[0]) {
            synchronized (lock) {
                try {
                    lock.wait(1000);
                } catch (InterruptedException e) {
                    log.debug("Interrupted while waiting for render event to be dispatched", e);
                }
            }
            if (!dispatched[0]) {
                log.warn("Render event was not dispatched within 1 second. The EDT might have consumed the event!");
            }
        }
    }

    private void waitUntilCanRender() {
        renderLock.lock();
        try {
            while (isRunning.get() && isPaused.get()) {
                log.trace("Paused, waiting for viewport to become visible and active");
                canRender.awaitUninterruptibly();
                log.trace("Viewport is now visible and active, resuming rendering");
            }
        } finally {
            renderLock.unlock();
        }
        if (refreshRate > 0) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastRenderTime;
            long timeToNextRender = (long) (1_000_000_000.0 / refreshRate) - elapsedTime;
            if (timeToNextRender > 0) {
                try {
                    Thread.sleep(timeToNextRender / 1_000_000, (int) (timeToNextRender % 1_000_000));
                } catch (InterruptedException e) {
                    log.debug("Interrupted while sleeping before next render", e);
                }
            }
            lastRenderTime = System.currentTimeMillis();
        }
    }

    private void updatePauseLater() {
        SwingUtilities.invokeLater(this::updatePause);
    }

    private void updatePause() {
        renderLock.lock();

        try {
            boolean value = shouldPause();
            if (isPaused.compareAndSet(!value, value)) {
                canRender.signal();
            }
        } finally {
            renderLock.unlock();
        }
    }

    private boolean shouldPause() {
        return viewport.getWidth() <= 0
            || viewport.getHeight() <= 0
            || !viewport.isShowing()
            || Stream.of(Window.getWindows()).noneMatch(Window::isActive);
    }

    private final class EventHandler implements HierarchyListener, ComponentListener, FocusListener {
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

        @Override
        public void focusGained(FocusEvent e) {
            updatePauseLater();
        }

        @Override
        public void focusLost(FocusEvent e) {
            updatePauseLater();
        }
    }
}
