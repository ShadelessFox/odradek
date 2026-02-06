package sh.adelessfox.odradek.viewer.model.viewport2;

import sh.adelessfox.odradek.ui.Disposable;
import sh.adelessfox.wgpuj.*;
import sh.adelessfox.wgpuj.Color;
import sh.adelessfox.wgpuj.Queue;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Optional;

public abstract class WgpuPanel extends JPanel implements Disposable {
    private final Instance instance;
    private final Adapter adapter;
    protected final Device device;
    protected final Queue queue;

    // TODO shitte
    protected final TextureFormat format = TextureFormat.RGBA8_UNORM;

    private Buffer backBuffer;
    private Texture backTexture;
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

        setup();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int clipWidth = getWidth();
        int clipHeight = getHeight();
        int bufferWidth = clipWidth + 127 & ~127;
        int bufferHeight = clipHeight + 127 & ~127;

        if (backTexture == null || backTexture.getWidth() != bufferWidth || backTexture.getHeight() != bufferHeight) {
            recreateRenderTexture(bufferWidth, bufferHeight);
            recreateRenderBuffer(bufferWidth, bufferHeight);
            recreateRenderImage(bufferWidth, bufferHeight);
        }

        try (
            var view = backTexture.createView();
            var encoder = device.createCommandEncoder(CommandEncoderDescriptor.builder().build())
        ) {
            var color = getBackground();
            var descriptor = RenderPassDescriptor.builder()
                .label("panel render pass")
                .addColorAttachments(RenderPassColorAttachment.builder()
                    .view(view)
                    .load(new LoadOp.Clear<>(new Color(
                        color.getRed() / 255.0f,
                        color.getGreen() / 255.0f,
                        color.getBlue() / 255.0f,
                        color.getAlpha() / 255.0f)))
                    .store(StoreOp.STORE)
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
                    .texture(backTexture)
                    .mipLevel(0)
                    .origin(new Origin3D(0, 0, 0))
                    .aspect(TextureAspect.ALL)
                    .build(),
                TexelCopyBufferInfo.builder()
                    .buffer(backBuffer)
                    .layout(TexelCopyBufferLayout.builder()
                        .offset(0)
                        .bytesPerRow(bufferWidth * 4)
                        .rowsPerImage(bufferHeight * 4)
                        .build())
                    .build(),
                new Extent3D(bufferWidth, bufferHeight, 1)
            );

            try (var encoded = encoder.finish(Optional.empty())) {
                queue.submit(encoded);
            }
        }

        long size = backBuffer.getSize();
        try (var mapped = backBuffer.map(instance, 0, size, MapMode.READ)) {
            var buffer = (DataBufferByte) image.getRaster().getDataBuffer();
            var dst = buffer.getData();
            var src = mapped.getMappedRange(0, size);

            for (int i = 0; i < size; i += 4) {
                dst[i/**/] = src.get(i + 3); // A
                dst[i + 1] = src.get(i + 2); // B
                dst[i + 2] = src.get(i + 1); // G
                dst[i + 3] = src.get(i/**/); // R
            }
        }

        g.drawImage(
            image,
            0, 0, clipWidth, clipHeight,
            0, 0, clipWidth, clipHeight,
            null);
    }

    protected abstract void setup();

    protected abstract void render(RenderPass pass);

    private void recreateRenderTexture(int width, int height) {
        if (backTexture != null) {
            backTexture.close();
        }
        var descriptor = TextureDescriptor.builder()
            .size(new Extent3D(width, height, 1))
            .mipLevelCount(1)
            .sampleCount(1)
            .dimension(TextureDimension.D2)
            .format(TextureFormat.RGBA8_UNORM)
            .addUsages(TextureUsage.COPY_SRC, TextureUsage.RENDER_ATTACHMENT)
            .build();
        backTexture = device.createTexture(descriptor);
    }

    private void recreateRenderBuffer(int width, int height) {
        if (backBuffer != null) {
            backBuffer.close();
        }
        var descriptor = BufferDescriptor.builder()
            .size(width * height * 4L)
            .addUsages(BufferUsage.COPY_DST, BufferUsage.MAP_READ)
            .mappedAtCreation(false)
            .build();
        backBuffer = device.createBuffer(descriptor);
    }

    private void recreateRenderImage(int width, int height) {
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
