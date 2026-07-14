package sh.adelessfox.odradek.opengl.awt;

import sh.adelessfox.odradek.opengl.Framebuffer;
import sh.adelessfox.odradek.ui.Disposable;
import wtf.reversed.toolbox.util.Check;

import java.awt.*;
import java.util.Optional;

abstract class SwapChain implements Disposable {
    private final SwapBuffer buffer = createSwapBuffer();
    private int width;
    private int height;

    /**
     * Presents the result of rendering to the {@link #getImage() backing image}.
     */
    void present() {
        ensureValid();
        buffer.present(this::render);
    }

    /**
     * Resizes the swap chain to the specified dimensions.
     */
    void resize(int width, int height) {
        int width2 = align256(Math.max(1, width));
        int height2 = align256(Math.max(1, height));

        if (this.width != width2 || this.height != height2) {
            this.width = width2;
            this.height = height2;
            buffer.resize(width2, height2);
        }
    }

    /**
     * Returns the backing image, if available. It might not be available
     * if the swap chain has not been resized and presented to yet.
     *
     * @return the backing image
     */
    Optional<Image> getImage() {
        return buffer.getImage();
    }

    @Override
    public void dispose() {
        buffer.dispose();
    }

    protected abstract void render(Framebuffer target);

    protected abstract SwapBuffer createSwapBuffer();

    private void ensureValid() {
        Check.state(width > 0 && height > 0, "swap chain needs to be resized before rendering");
    }

    private static int align256(int value) {
        return (value + 255) & ~255;
    }
}
