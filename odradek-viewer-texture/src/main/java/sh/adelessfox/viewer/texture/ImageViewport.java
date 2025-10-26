package sh.adelessfox.viewer.texture;

import javax.swing.*;
import java.awt.*;

public class ImageViewport extends JViewport {
    private static final int TILE_SIZE = 8;

    public ImageViewport(ImageView view) {
        final JPanel inner = new JPanel();
        inner.setLayout(new GridBagLayout());
        inner.setOpaque(false);
        inner.add(view);

        setView(inner);
        setBackground(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        for (int x = 0; x < getWidth(); x += TILE_SIZE) {
            for (int y = 0; y < getHeight(); y += TILE_SIZE) {
                final int i = (x + y % (TILE_SIZE * 2)) % (TILE_SIZE * 2);
                g.setColor(i > 0 ? Color.GRAY : Color.DARK_GRAY);
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }
    }
}
