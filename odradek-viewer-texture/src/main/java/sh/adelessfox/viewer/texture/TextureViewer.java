package sh.adelessfox.viewer.texture;

import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.util.Arrays;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteOrder;
import java.util.Optional;

public class TextureViewer implements Viewer<Texture> {
    private static final float ZOOM_STEP = 0.2f;
    private static final float ZOOM_MIN_LEVEL = (float) Math.pow(2, -5);
    private static final float ZOOM_MAX_LEVEL = (float) Math.pow(2, 7);

    @Override
    public JComponent createComponent(Texture texture) {
        var imagePanel = new ImagePanel();
        imagePanel.setImage(createImage(texture));

        var scrollPane = new JScrollPane();
        scrollPane.setViewport(new ImageViewport(imagePanel));
        scrollPane.addMouseWheelListener(e -> {
            float step = ZOOM_STEP * (float) -e.getPreciseWheelRotation();
            float oldZoom = imagePanel.getZoom();
            float newZoom = Math.clamp((float) Math.exp(Math.log(oldZoom) + step), ZOOM_MIN_LEVEL, ZOOM_MAX_LEVEL);

            if (oldZoom == newZoom) {
                return;
            }

            var viewport = scrollPane.getViewport();
            var point = SwingUtilities.convertPoint(viewport, e.getX(), e.getY(), imagePanel);
            var rect = viewport.getViewRect();
            rect.x = (int) Math.round(point.getX() * newZoom / oldZoom - point.getX() + rect.getX());
            rect.y = (int) Math.round(point.getY() * newZoom / oldZoom - point.getY() + rect.getY());

            if (newZoom > oldZoom) {
                imagePanel.setZoom(newZoom);
                imagePanel.scrollRectToVisible(rect);
            } else {
                imagePanel.scrollRectToVisible(rect);
                imagePanel.setZoom(newZoom);
            }
        });

        SwingUtilities.invokeLater(imagePanel::fit);

        return scrollPane;
    }

    private static BufferedImage createImage(Texture texture) {
        var converted = texture.convert(TextureFormat.B8G8R8A8_UNORM);
        var surface = converted.surfaces().getFirst();

        var image = new BufferedImage(surface.width(), surface.height(), BufferedImage.TYPE_INT_ARGB);
        var imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0, o = 0, len = surface.data().length; i < len; i += 4, o++) {
            imageData[o] = Arrays.getInt(surface.data(), i, ByteOrder.LITTLE_ENDIAN);
        }

        return image;
    }

    @Override
    public String name() {
        return "Texture";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:image");
    }
}
