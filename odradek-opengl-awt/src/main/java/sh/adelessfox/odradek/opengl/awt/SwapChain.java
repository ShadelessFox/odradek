package sh.adelessfox.odradek.opengl.awt;

import sh.adelessfox.odradek.opengl.Framebuffer;
import sh.adelessfox.odradek.ui.Disposable;
import wtf.reversed.toolbox.util.Check;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

abstract class SwapChain implements Disposable {
    private static final int BUFFER_COUNT = 2;

    private final List<SwapBuffer> buffers = IntStream.range(0, BUFFER_COUNT)
        .mapToObj(_ -> createSwapBuffer())
        .toList();

    private int width;
    private int height;
    private int frontBufferIndex;
    private int stagedBufferIndex = -1;
    private boolean hasImage;

    /**
     * Presents the result of rendering to the {@link #getImage() backing image}.
     */
    void present() {
        ensureValid();

        if (stagedBufferIndex >= 0) {
            buffers.get(stagedBufferIndex).awaitReadback();
            frontBufferIndex = stagedBufferIndex;
            stagedBufferIndex = -1;
            hasImage = true;
        }

        var renderBufferIndex = (frontBufferIndex + 1) % buffers.size();
        var renderBuffer = buffers.get(renderBufferIndex);
        renderBuffer.render(this::render);
        renderBuffer.beginReadback();

        if (!hasImage) {
            renderBuffer.awaitReadback();
            frontBufferIndex = renderBufferIndex;
            hasImage = true;
        } else {
            stagedBufferIndex = renderBufferIndex;
        }
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

            for (SwapBuffer buffer : buffers) {
                buffer.resize(width2, height2);
            }

            stagedBufferIndex = -1;
            hasImage = false;
        }
    }

    /**
     * Returns the backing image, if available. It might not be available
     * if the swap chain has not been resized and presented to yet.
     *
     * @return the backing image
     */
    Optional<Image> getImage() {
        return buffers.get(frontBufferIndex).getImage();
    }

    @Override
    public void dispose() {
        for (SwapBuffer buffer : buffers) {
            buffer.dispose();
        }
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
