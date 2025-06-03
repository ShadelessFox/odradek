package sh.adelessfox.viewer.texture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ImagePanel extends JComponent implements Scrollable {
    private static final Logger log = LoggerFactory.getLogger(ImagePanel.class);
    private BufferedImage image;
    private float zoom;

    public ImagePanel() {
        Robot robot = null;

        try {
            robot = new Robot();
        } catch (AWTException e) {
            log.warn("Can't create robot", e);
        }

        Handler handler = new Handler(robot);
        addMouseListener(handler);
        addMouseMotionListener(handler);

        reset();
    }

    public void fit() {
        if (image == null) {
            return;
        }
        setZoom(computeFitZoom());
    }


    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        if (this.image != image) {
            reset();
            this.image = image;
            update();
        }
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        if (this.zoom != zoom) {
            this.zoom = Math.max(0.0f, zoom);
            update();
        }
    }

    private void reset() {
        this.image = null;
        this.zoom = 1.0f;
    }

    private void update() {
        revalidate();
        repaint();
    }

    private float computeFitZoom() {
        var container = SwingUtilities.getAncestorOfClass(JViewport.class, this).getParent();
        var viewport = container.getSize();
        var insets = container.getInsets();
        viewport.width -= insets.left + insets.right;
        viewport.height -= insets.top + insets.bottom;

        float rs = (float) viewport.width / (float) viewport.height;
        float ri = (float) image.getWidth() / (float) image.getHeight();

        if (rs > ri) {
            return (float) viewport.height / (float) image.getHeight();
        } else {
            return (float) viewport.width / (float) image.getWidth();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image != null) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            g2.scale(zoom, zoom);
            g2.drawImage(image, 0, 0, null);

            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (image != null) {
            return new Dimension(Math.round(image.getWidth() * zoom), Math.round(image.getHeight() * zoom));
        } else {
            return new Dimension();
        }
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getScrollableBlockIncrement(visibleRect, orientation, direction) / 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width;
        } else {
            return visibleRect.height;
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private class Handler extends MouseAdapter {
        private final Robot robot;
        private Point origin;

        private Handler(Robot robot) {
            this.robot = robot;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                origin = e.getPoint();
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                origin = null;
                setCursor(null);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (origin == null) {
                return;
            }

            var viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, ImagePanel.this);
            var view = viewport.getViewRect();

            var mouse = e.getLocationOnScreen();
            var bounds = new Rectangle(viewport.getLocationOnScreen(), viewport.getSize());

            if (robot != null && !bounds.contains(mouse)) {
                if (mouse.x >= bounds.x + bounds.width) {
                    mouse.x = bounds.x + 1;
                } else if (mouse.x < bounds.x) {
                    mouse.x = bounds.x + bounds.width - 1;
                }

                if (mouse.y >= bounds.y + bounds.height) {
                    mouse.y = bounds.y + 1;
                } else if (mouse.y < bounds.y) {
                    mouse.y = bounds.y + bounds.height - 1;
                }

                robot.mouseMove(mouse.x, mouse.y);
                origin.x = mouse.x;
                origin.y = mouse.y;

                SwingUtilities.convertPointFromScreen(origin, ImagePanel.this);
            } else {
                view.x += origin.x - e.getX();
                view.y += origin.y - e.getY();
            }

            scrollRectToVisible(view);
        }
    }
}
