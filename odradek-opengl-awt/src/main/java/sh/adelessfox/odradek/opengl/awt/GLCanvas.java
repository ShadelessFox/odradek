package sh.adelessfox.odradek.opengl.awt;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(GLCanvas.class);

    private final PlatformGLCanvas canvas;

    private final List<GLEventListener> listeners = new ArrayList<>();
    private final GLData data;
    private final GLData effective = new GLData();

    long context;
    private int framebufferWidth;
    private int framebufferHeight;

    public GLCanvas(GLData data) {
        PlatformGLCanvas canvas;
        try {
            canvas = createPlatformCanvas();
        } catch (Throwable e) {
            log.error("Unable to create platform canvas", e);
            canvas = null;
        }

        this.data = data;
        this.canvas = canvas;

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                var transform = getGraphicsConfiguration().getDefaultTransform();
                framebufferWidth = (int) (getWidth() * transform.getScaleX());
                framebufferHeight = (int) (getHeight() * transform.getScaleY());
            }
        });
    }

    private static PlatformGLCanvas createPlatformCanvas() {
        return switch (Platform.get()) {
            case WINDOWS -> new PlatformWin32GLCanvas();
            case LINUX -> new PlatformLinuxGLCanvas();
            default -> throw new UnsupportedOperationException("Platform " + Platform.get() + " not yet supported");
        };
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
    public void paint(Graphics g) {
        if (canvas == null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.clearRect(0, 0, getWidth(), getHeight());
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawCenteredString(g2, "Unable to create graphics context; refer to logs for more details", getWidth(), getHeight());
            g2.dispose();
        } else {
            render();
        }
    }

    private static void drawCenteredString(Graphics g, String text, int width, int height) {
        FontMetrics fm = g.getFontMetrics();

        int x = 0;
        int y = (height - fm.getHeight() + 1) / 2 + fm.getAscent();

        if (text.indexOf('\n') >= 0) {
            for (String line : text.split("\n")) {
                drawCenteredString(g, line, x, y, width);
                y += fm.getHeight();
            }
        } else {
            drawCenteredString(g, text, x, y, width);
        }
    }

    private static void drawCenteredString(Graphics g, String text, int x, int y, int width) {
        g.drawString(text, x + (width - g.getFontMetrics().stringWidth(text)) / 2, y);
    }

    @Override
    public void update(Graphics g) {
        // This override makes so that the canvas doesn't flicker by removing super's clearRect
        paint(g);
    }

    public void render() {
        ensureEDT();

        if (canvas == null) {
            return;
        }

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
        ensureEDT();
        listeners.add(listener);
        if (context != 0) {
            submit(listener::onCreate);
        }
    }

    public void removeGLEventListener(GLEventListener listener) {
        ensureEDT();
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
    private void submit(Runnable runnable) {
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

    private static void ensureEDT() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("AWTGLCanvas methods must be called from the Event Dispatch Thread");
        }
    }

    private void notifyListeners(Consumer<GLEventListener> consumer) {
        for (GLEventListener listener : listeners) {
            consumer.accept(listener);
        }
    }
}
