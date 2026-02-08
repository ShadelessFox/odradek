package sh.adelessfox.odradek.viewer.model.viewport2;

import sh.adelessfox.odradek.ui.Disposable;
import sh.adelessfox.wgpuj.*;
import sh.adelessfox.wgpuj.Color;
import sh.adelessfox.wgpuj.Queue;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

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
        var instanceDescriptor = InstanceDescriptor.builder()
            .addFlags(InstanceFlag.DEBUG, InstanceFlag.VALIDATION)
            .build();
        var deviceDescriptor = DeviceDescriptor.builder()
            .build();

        instance = Instance.create(instanceDescriptor);
        adapter = instance.requestAdapter();
        device = adapter.requestDevice(deviceDescriptor);
        queue = device.getQueue();
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

        try (
            var colorTextureView = colorTexture.createView();
            var depthTextureView = depthTexture.createView();
            var encoder = device.createCommandEncoder(CommandEncoderDescriptor.builder().build())
        ) {
            var descriptor = RenderPassDescriptor.builder()
                .label("panel render pass")
                .addColorAttachments(RenderPassColorAttachment.builder()
                    .view(colorTextureView)
                    .ops(new Operations<>(
                        new LoadOp.Clear<>(new Color(
                            clear.getRed() / 255.0f,
                            clear.getGreen() / 255.0f,
                            clear.getBlue() / 255.0f,
                            clear.getAlpha() / 255.0f)),
                        StoreOp.STORE
                    ))
                    .build())
                .depthStencilAttachment(RenderPassDepthStencilAttachment.builder()
                    .view(depthTextureView)
                    .depthOps(new Operations<>(
                        new LoadOp.Clear<>(1.0f),
                        StoreOp.STORE
                    ))
                    .build())
                .build();

            try (var pass = encoder.beginRenderPass(descriptor)) {
                pass.setViewport(0, 0, clipWidth, clipHeight, 0, 1);
                render(pass);
                pass.end();
            }

            // Copy the rendered texture to the buffer
            encoder.copyTextureToBuffer(
                TexelCopyTextureInfo.builder()
                    .texture(colorTexture)
                    .mipLevel(0)
                    .origin(new Origin3D(0, 0, 0))
                    .aspect(TextureAspect.ALL)
                    .build(),
                TexelCopyBufferInfo.builder()
                    .buffer(colorBuffer)
                    .layout(TexelCopyBufferLayout.builder()
                        .offset(0)
                        .bytesPerRow(bufferWidth * 4)
                        .rowsPerImage(bufferHeight * 4)
                        .build())
                    .build(),
                new Extent3D(bufferWidth, bufferHeight, 1)
            );

            try (var encoded = encoder.finish()) {
                queue.submit(encoded);
            }
        }

        long size = colorBuffer.getSize();
        try (var mapped = colorBuffer.map(0, size, MapMode.READ)) {
            var buffer = (DataBufferByte) image.getRaster().getDataBuffer();
            var dst = buffer.getData();
            var src = mapped.asBuffer(0, size);

            for (int i = 0; i < size; i += 4) {
                dst[i/**/] = src.get(i + 3); // A
                dst[i + 1] = src.get(i + 2); // B
                dst[i + 2] = src.get(i + 1); // G
                dst[i + 3] = src.get(i/**/); // R
            }
        }

        g.setColor(clear);
        g.fillRect(0, 0, clipWidth, clipHeight);
        g.drawImage(
            image,
            0, 0, clipWidth, clipHeight,
            0, 0, clipWidth, clipHeight,
            null);
    }

    protected abstract void render(RenderPass pass);

    private void recreateColorTexture(int width, int height) {
        if (colorTexture != null) {
            colorTexture.close();
        }
        var descriptor = TextureDescriptor.builder()
            .size(new Extent3D(width, height, 1))
            .mipLevelCount(1)
            .sampleCount(1)
            .dimension(TextureDimension.D2)
            .format(COLOR_ATTACHMENT_FORMAT)
            .addUsages(TextureUsage.COPY_SRC, TextureUsage.RENDER_ATTACHMENT)
            .build();
        colorTexture = device.createTexture(descriptor);
    }

    private void recreateDepthTexture(int width, int height) {
        if (depthTexture != null) {
            depthTexture.close();
        }
        var descriptor = TextureDescriptor.builder()
            .size(new Extent3D(width, height, 1))
            .mipLevelCount(1)
            .sampleCount(1)
            .dimension(TextureDimension.D2)
            .format(DEPTH_ATTACHMENT_FORMAT)
            .addUsages(TextureUsage.RENDER_ATTACHMENT)
            .build();
        depthTexture = device.createTexture(descriptor);
    }

    private void recreateColorBuffer(int width, int height) {
        if (colorBuffer != null) {
            colorBuffer.close();
        }
        var descriptor = BufferDescriptor.builder()
            .size(width * height * 4L)
            .addUsages(BufferUsage.COPY_DST, BufferUsage.MAP_READ)
            .mappedAtCreation(false)
            .build();
        colorBuffer = device.createBuffer(descriptor);
    }

    private void recreateImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    }

    @Override
    public void dispose() {
        queue.close();
        device.close();
        adapter.close();
        instance.close();
    }
}
