package sh.adelessfox.odradek.opengl.awt;

import sh.adelessfox.odradek.opengl.Buffer;
import sh.adelessfox.odradek.opengl.Framebuffer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Optional;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL44.GL_CLIENT_STORAGE_BIT;

final class BlitSwapBuffer extends SwapBuffer {
    private final int samples;

    private BufferedImage image; // Image to display
    private int[] raster; // Its raster data, cached, because DataBuffer#getData() is expensive

    private Framebuffer destFramebuffer; // Framebuffer to render to, possibly multisampled
    private Framebuffer blitFramebuffer; // Framebuffer to blit to, used for reading pixels, never multisampled
    private Buffer pbo; // Pixel Buffer Object to read pixels into, used for async readback
    private long readbackFence; // Fence to wait for readback completion

    BlitSwapBuffer(int samples) {
        this.samples = samples;
    }

    @Override
    void render(Consumer<Framebuffer> renderer) {
        renderer.accept(destFramebuffer);
    }

    @Override
    Optional<Image> getImage() {
        return Optional.ofNullable(image);
    }

    @Override
    void resize(int width, int height) {
        deleteFbos();
        deleteFence();
        deletePbo();

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        raster = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        destFramebuffer = Framebuffer.create(width, height, samples);
        blitFramebuffer = Framebuffer.create(width, height, 0);

        pbo = new Buffer();
        pbo.allocate(width * height * Integer.BYTES, GL_MAP_READ_BIT | GL_CLIENT_STORAGE_BIT);
    }

    @Override
    void beginReadback() {
        ensureNoPendingReadback();

        int width = blitFramebuffer.width();
        int height = blitFramebuffer.height();

        destFramebuffer.blitTo(blitFramebuffer, GL_COLOR_BUFFER_BIT, GL_NEAREST);
        blitFramebuffer.readPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, pbo, 0L);
        readbackFence = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
    }

    @Override
    void awaitReadback() {
        if (readbackFence == 0) {
            return;
        }

        int waitResult = glClientWaitSync(readbackFence, GL_SYNC_FLUSH_COMMANDS_BIT, GL_TIMEOUT_IGNORED);
        if (waitResult == GL_WAIT_FAILED) {
            throw new IllegalStateException("failed to wait for swap buffer readback");
        }

        var buffer = pbo.map(GL_MAP_READ_BIT).orElse(null);
        if (buffer == null) {
            throw new IllegalStateException("swap buffer readback buffer is not mappable");
        }

        buffer.asIntBuffer().get(0, raster);

        if (!pbo.unmap()) {
            throw new IllegalStateException("failed to unmap swap buffer readback buffer");
        }

        deleteFence();
    }

    @Override
    public void dispose() {
        deleteFbos();
        deleteFence();
        deletePbo();
    }

    private void ensureNoPendingReadback() {
        if (readbackFence != 0) {
            throw new IllegalStateException("swap buffer readback has not been consumed yet");
        }
    }

    private void deleteFbos() {
        if (destFramebuffer != null) {
            destFramebuffer.dispose();
        }
        if (blitFramebuffer != null) {
            blitFramebuffer.dispose();
        }
    }

    private void deleteFence() {
        if (readbackFence != 0) {
            glDeleteSync(readbackFence);
            readbackFence = 0;
        }
    }

    private void deletePbo() {
        if (pbo != null) {
            pbo.dispose();
            pbo = null;
        }
    }
}
