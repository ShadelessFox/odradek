package sh.adelessfox.odradek.opengl.awt;

import sh.adelessfox.odradek.opengl.Framebuffer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.util.Optional;
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
        renderer.accept(destFramebuffer);
        blit();
    }

    @Override
    Optional<Image> getImage() {
        return Optional.ofNullable(image);
    }

    @Override
    void resize(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        raster = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        buffer = ByteBuffer.allocateDirect(width * height * 4);

        if (destFramebuffer != null) {
            destFramebuffer.dispose();
        }
        if (blitFramebuffer != null) {
            blitFramebuffer.dispose();
        }

        destFramebuffer = Framebuffer.create(width, height, samples);
        blitFramebuffer = Framebuffer.create(width, height, 0);
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
}
