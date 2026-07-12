package sh.adelessfox.odradek.opengl.awt;

import sh.adelessfox.odradek.ui.Disposable;

import java.awt.*;

abstract class SwapChain implements Disposable {
    private final SwapBuffer buffer;

    SwapChain() {
        buffer = createSwapBuffer();
    }

    void present() {
        buffer.present(this::render);
    }

    void resize(int width, int height) {
        buffer.resize(width, height);
    }

    Image getImage() {
        return buffer.getImage();
    }

    @Override
    public void dispose() {
        buffer.dispose();
    }

    protected abstract void render(sh.adelessfox.odradek.opengl.Framebuffer target);

    protected abstract SwapBuffer createSwapBuffer();
}
