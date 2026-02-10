package sh.adelessfox.odradek.viewer.model.viewport2;

import sh.adelessfox.odradek.ui.Disposable;
import sh.adelessfox.wgpuj.*;
import sh.adelessfox.wgpuj.objects.*;
import sh.adelessfox.wgpuj.objects.Queue;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteOrder;

public abstract class WgpuPanel extends JPanel implements Disposable {
    public static final TextureFormat COLOR_ATTACHMENT_FORMAT = TextureFormat.RGBA8_UNORM;
    public static final TextureFormat DEPTH_ATTACHMENT_FORMAT = TextureFormat.DEPTH24_PLUS;

    private final Instance instance;
    private final Adapter adapter;
    protected final Device device;
    protected final Queue queue;

    private Texture colorTexture;
    private Texture depthTexture;
    private Buffer colorBuffer;

    private BufferedImage image;

    public WgpuPanel() {
        var instanceDescriptor = ImmutableInstanceDescriptor.builder()
            .addFlags(InstanceFlag.DEBUG, InstanceFlag.VALIDATION)
            .build();
        var deviceDescriptor = ImmutableDeviceDescriptor.builder()
            .build();

        instance = Instance.create(instanceDescriptor);
        adapter = instance.requestAdapter();
        device = adapter.requestDevice(deviceDescriptor);
        queue = device.getQueue();

        setDoubleBuffered(false);
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int clipWidth = getWidth();
        int clipHeight = getHeight();

        // This helps reduce the number of texture recreations by
        // aligning buffers to 128 so that small resizes don't
        // invalidate old buffers, as well as ensuring that
        // bytesPerRow is a multiple of 256, enforced by wgpu
        int bufferWidth = clipWidth + 127 & ~127;
        int bufferHeight = clipHeight + 127 & ~127;

        if (colorTexture == null || colorTexture.getWidth() != bufferWidth || colorTexture.getHeight() != bufferHeight) {
            recreateColorTexture(bufferWidth, bufferHeight);
            recreateDepthTexture(bufferWidth, bufferHeight);
            recreateColorBuffer(bufferWidth, bufferHeight);
            recreateImage(bufferWidth, bufferHeight);
        }

        var clear = getBackground();
        submit(clear, clipWidth, clipHeight, bufferWidth, bufferHeight);
        copyTexture();
        blitTexture(g, clear, clipWidth, clipHeight);
    }

    private void submit(Color clear, int clipWidth, int clipHeight, int bufferWidth, int bufferHeight) {
        try (
            var colorTextureView = colorTexture.createView();
            var depthTextureView = depthTexture.createView();
            var commandEncoder = device.createCommandEncoder(ImmutableCommandEncoderDescriptor.builder()
                .label("offscreen command encoder")
                .build())
        ) {
            var descriptor = ImmutableRenderPassDescriptor.builder()
                .label("panel render pass")
                .addColorAttachments(ImmutableRenderPassColorAttachment.builder()
                    .view(colorTextureView)
                    .ops(ImmutableOperations.of(
                        ImmutableOperations.Clear.of(ImmutableColor.of(
                            clear.getRed() / 255.0f,
                            clear.getGreen() / 255.0f,
                            clear.getBlue() / 255.0f,
                            clear.getAlpha() / 255.0f)),
                        Operations.StoreOp.STORE))
                    .build())
                .depthStencilAttachment(ImmutableRenderPassDepthStencilAttachment.builder()
                    .view(depthTextureView)
                    .depthOps(ImmutableOperations.of(
                        ImmutableOperations.Clear.of(1.0f),
                        Operations.StoreOp.STORE))
                    .build())
                .build();

            try (var pass = commandEncoder.beginRenderPass(descriptor)) {
                pass.setViewport(0, 0, clipWidth, clipHeight, 0, 1);
                render(pass);
                pass.end();
            }

            // Copy the rendered texture to the buffer
            commandEncoder.copyTextureToBuffer(
                ImmutableTexelCopyTextureInfo.of(colorTexture),
                ImmutableTexelCopyBufferInfo.builder()
                    .buffer(colorBuffer)
                    .layout(ImmutableTexelCopyBufferLayout.builder()
                        .bytesPerRow(bufferWidth * 4)
                        .rowsPerImage(bufferHeight * 4)
                        .build())
                    .build(),
                ImmutableExtent3D.builder()
                    .width(bufferWidth)
                    .height(bufferHeight)
                    .build());

            try (var encoded = commandEncoder.finish()) {
                queue.submit(encoded);
            }
        }
    }

    private void blitTexture(Graphics g, Color clear, int clipWidth, int clipHeight) {
        g.setColor(clear);
        g.fillRect(0, 0, clipWidth, clipHeight);
        g.drawImage(
            image,
            0, 0, clipWidth, clipHeight,
            0, 0, clipWidth, clipHeight,
            null);
    }

    private void copyTexture() {
        long size = colorBuffer.getSize();
        try (var mapped = colorBuffer.map(0, size, MapMode.READ)) {
            var dst = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            var src = mapped.asBuffer(0, size).order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < dst.length; i++) {
                int rgba = src.getInt(i << 2);
                int bgra = rgba & 0xFF00FF00 | (rgba & 0x00FF0000) >> 16 | (rgba & 0x000000FF) << 16;
                dst[i] = bgra;
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    protected abstract void render(RenderPass pass);

    private void recreateColorTexture(int width, int height) {
        if (colorTexture != null) {
            colorTexture.close();
        }
        var descriptor = ImmutableTextureDescriptor.builder()
            .size(ImmutableExtent3D.builder()
                .width(width)
                .height(height)
                .build())
            .format(COLOR_ATTACHMENT_FORMAT)
            .addUsages(TextureUsage.COPY_SRC, TextureUsage.RENDER_ATTACHMENT)
            .build();
        colorTexture = device.createTexture(descriptor);
    }

    private void recreateDepthTexture(int width, int height) {
        if (depthTexture != null) {
            depthTexture.close();
        }
        var descriptor = ImmutableTextureDescriptor.builder()
            .size(ImmutableExtent3D.builder()
                .width(width)
                .height(height)
                .build())
            .format(DEPTH_ATTACHMENT_FORMAT)
            .addUsages(TextureUsage.RENDER_ATTACHMENT)
            .build();
        depthTexture = device.createTexture(descriptor);
    }

    private void recreateColorBuffer(int width, int height) {
        if (colorBuffer != null) {
            colorBuffer.close();
        }
        var descriptor = ImmutableBufferDescriptor.builder()
            .size(width * height * 4L)
            .addUsages(BufferUsage.COPY_DST, BufferUsage.MAP_READ)
            .mappedAtCreation(false)
            .build();
        colorBuffer = device.createBuffer(descriptor);
    }

    private void recreateImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void dispose() {
        queue.close();
        device.close();
        adapter.close();
        instance.close();
    }
}
