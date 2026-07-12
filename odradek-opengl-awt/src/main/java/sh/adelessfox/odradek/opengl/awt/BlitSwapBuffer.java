package sh.adelessfox.odradek.opengl.awt;

import sh.adelessfox.odradek.opengl.Framebuffer;
import wtf.reversed.toolbox.util.Check;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;

final class BlitSwapBuffer extends SwapBuffer {
    private final int samples;

    private BufferedImage image; // Image to display
    private int[] raster; // Its raster data, cached, because DataBuffer#getData() is expensive

    private ByteBuffer buffer; // Buffer to store pixel data read from the framebuffer
    private Framebuffer destFramebuffer; // Framebuffer to render to, possibly multisampled
    private Framebuffer blitFramebuffer; // Framebuffer to blit to, used for reading pixels, never multisampled

    BlitSwapBuffer(int samples) {
        this.samples = samples;
    }

    @Override
    void present(Consumer<Framebuffer> renderer) {
        ensureValid();
        renderer.accept(destFramebuffer);
        blit();
    }

    @Override
    Image getImage() {
        ensureValid();
        return image;
    }

    @Override
    void resize(int width, int height) {
        var width2 = align256(width);
        var height2 = align256(height);
        if (image != null && image.getWidth() == width2 && image.getHeight() == height2) {
            return;
        }

        image = new BufferedImage(width2, height2, BufferedImage.TYPE_INT_ARGB);
        raster = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        buffer = ByteBuffer.allocateDirect(width2 * height2 * 4);

        if (destFramebuffer != null) {
            destFramebuffer.dispose();
        }
        if (blitFramebuffer != null) {
            blitFramebuffer.dispose();
        }

        destFramebuffer = Framebuffer.create(width2, height2, samples);
        blitFramebuffer = Framebuffer.create(width2, height2, 0);
    }

    @Override
    public void dispose() {
        if (blitFramebuffer != null) {
            blitFramebuffer.dispose();
        }
        if (destFramebuffer != null) {
            destFramebuffer.dispose();
        }
    }

    private void blit() {
        int width = blitFramebuffer.width();
        int height = blitFramebuffer.height();

        destFramebuffer.blitTo(blitFramebuffer, GL_COLOR_BUFFER_BIT, GL_NEAREST);
        blitFramebuffer.readPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        for (int i = 0, size = width * height; i < size; i++) {
            int rgba = buffer.getInt(i * 4);
            int argb = ((rgba & 0xFF) << 24) | (rgba >>> 8);
            raster[i] = argb;
        }
    }

    private void ensureValid() {
        Check.state(destFramebuffer != null, "swap buffer needs to be resized before rendering");
    }

    private static int align256(int value) {
        return (value + 255) & ~255;
    }
}
