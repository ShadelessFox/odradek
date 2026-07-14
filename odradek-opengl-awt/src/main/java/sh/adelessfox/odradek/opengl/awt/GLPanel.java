package sh.adelessfox.odradek.opengl.awt;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import sh.adelessfox.odradek.opengl.Framebuffer;
import sh.adelessfox.odradek.opengl.context.GLContext;
import sh.adelessfox.odradek.opengl.context.GLProfile;
import sh.adelessfox.odradek.ui.Disposable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GLPanel extends JPanel implements Disposable {
    private final List<GLEventListener> listeners = new ArrayList<>();
    private final GLContext context;
    private final SwapChain swapChain;
    private GLCapabilities capabilities;

    public GLPanel(GLProfile profile, int major, int minor, boolean debug, int samples) {
        this.context = GLContext.create(profile, major, minor, debug);
        this.swapChain = new SwapChain() {
            @Override
            protected void render(Framebuffer target) {
                try (var _ = target.bind()) {
                    notifyListeners(GLEventListener::onRender);
                }
            }

            @Override
            protected SwapBuffer createSwapBuffer() {
                return new BlitSwapBuffer(samples);
            }
        };
    }

    public void render() {
        runInContext(() -> {
            swapChain.resize(getWidth(), getHeight());
            swapChain.present();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        swapChain.getImage().ifPresent(image -> {
            int width = getWidth();
            int height = getHeight();

            g.setColor(getBackground());
            g.fillRect(0, 0, width, height);
            g.drawImage(image, 0, 0, width, height, 0, height, width, 0, null);
        });
    }

    @Override
    public void dispose() {
        runInContext(() -> {
            notifyListeners(GLEventListener::onDestroy);
            swapChain.dispose();
            context.dispose();
        });
    }

    public void addGLEventListener(GLEventListener listener) {
        listeners.add(listener);
    }

    public void removeGLEventListener(GLEventListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Consumer<GLEventListener> consumer) {
        for (GLEventListener listener : listeners) {
            consumer.accept(listener);
        }
    }

    private void runInContext(Runnable runnable) {
        context.makeCurrent();

        if (capabilities == null) {
            capabilities = GL.createCapabilities();
            notifyListeners(GLEventListener::onCreate);
        } else {
            GL.setCapabilities(capabilities);
        }

        try {
            runnable.run();
        } finally {
            context.clearCurrent();
            GL.setCapabilities(null);
        }
    }
}
