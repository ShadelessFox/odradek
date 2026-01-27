package sh.adelessfox.odradek.viewer.texture.view;

import javax.swing.*;
import java.awt.*;

/**
 * A zoomable, pannable panel for displaying images.
 */
public final class ImagePanel extends JPanel {
    private static final float ZOOM_STEP = 0.2f;
    private static final float ZOOM_MIN_LEVEL = (float) Math.pow(2, -5);
    private static final float ZOOM_MAX_LEVEL = (float) Math.pow(2, 7);

    public ImagePanel(ImageView view) {
        var viewport = new ImageViewport(view);

        var pane = new JScrollPane();
        pane.setViewport(viewport);
        pane.addMouseWheelListener(e -> {
            float step = ZOOM_STEP * (float) -e.getPreciseWheelRotation();
            float oldZoom = view.getZoom();
            float newZoom = Math.clamp((float) Math.exp(Math.log(oldZoom) + step), ZOOM_MIN_LEVEL, ZOOM_MAX_LEVEL);

            if (oldZoom == newZoom) {
                return;
            }

            var point = SwingUtilities.convertPoint(viewport, e.getX(), e.getY(), view);
            var rect = viewport.getViewRect();
            rect.x = (int) Math.round(point.getX() * newZoom / oldZoom - point.getX() + rect.getX());
            rect.y = (int) Math.round(point.getY() * newZoom / oldZoom - point.getY() + rect.getY());

            // Zoom is now controlled by the user
            view.setZoomToFit(false);

            if (newZoom > oldZoom) {
                view.setZoom(newZoom);
                view.scrollRectToVisible(rect);
            } else {
                view.scrollRectToVisible(rect);
                view.setZoom(newZoom);
            }
        });

        setLayout(new BorderLayout());
        add(pane, BorderLayout.CENTER);
    }
}
