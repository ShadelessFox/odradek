package sh.adelessfox.odradek.opengl.awt;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Platform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A {@link Canvas} that can be drawn onto using OpenGL.
 *
 * @author Kai Burjack
 */
public final class GLCanvas extends Canvas {
    private static final ThreadLocal<GLCapabilities> capabilities = new ThreadLocal<>();

    private final PlatformGLCanvas canvas = switch (Platform.get()) {
        case WINDOWS -> new PlatformWin32GLCanvas();
        case LINUX -> new PlatformLinuxGLCanvas();
        default -> throw new UnsupportedOperationException("Platform " + Platform.get() + " not yet supported");
    };

    private final List<GLEventListener> listeners = new ArrayList<>();
    private final GLData data;
    private final GLData effective = new GLData();

    long context;
    private int framebufferWidth;
    private int framebufferHeight;

    public GLCanvas(GLData data) {
        this.data = data;

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                var transform = getGraphicsConfiguration().getDefaultTransform();
                framebufferWidth = (int) (getWidth() * transform.getScaleX());
                framebufferHeight = (int) (getHeight() * transform.getScaleY());
            }
        });
    }

    @Override
    public void removeNotify() {
        if (context != 0) {
            submit(() -> notifyListeners(GLEventListener::onDestroy));
            canvas.dispose();
            context = 0;
        }

        super.removeNotify();
    }

    @Override
    public void update(Graphics g) {
        update();
    }

    public void update() {
        boolean needsInitialization = context == 0;
        if (needsInitialization) {
            context = canvas.create(this, data, effective);
        }

        submit(() -> {
            if (needsInitialization) {
                notifyListeners(GLEventListener::onCreate);
            }
            notifyListeners(GLEventListener::onRender);
            canvas.swapBuffers();
        });
    }

    public void addGLEventListener(GLEventListener listener) {
        listeners.add(listener);
        if (context != 0) {
            submit(listener::onCreate);
        }
    }

    public void removeGLEventListener(GLEventListener listener) {
        listeners.remove(listener);
        if (context != 0) {
            submit(listener::onDestroy);
        }
    }

    public int getFramebufferWidth() {
        return framebufferWidth;
    }

    public int getFramebufferHeight() {
        return framebufferHeight;
    }

    /**
     * Executes the given {@code runnable} within a OpenGL context, ensuring it's created beforehand.
     *
     * @param runnable the runnable to execute
     */
    public void submit(Runnable runnable) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("AWTGLCanvas methods must be called from the Event Dispatch Thread");
        }

        canvas.lock();
        canvas.makeCurrent(context);

        try {
            if (capabilities.get() == null) {
                capabilities.set(GL.createCapabilities());
            } else {
                GL.setCapabilities(capabilities.get());
            }

            runnable.run();
        } finally {
            GL.setCapabilities(null);

            canvas.makeCurrent(0);
            canvas.unlock();
        }
    }

    private void notifyListeners(Consumer<GLEventListener> consumer) {
        for (GLEventListener listener : listeners) {
            consumer.accept(listener);
        }
    }
}
