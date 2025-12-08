package sh.adelessfox.odradek.ui.util;

import com.formdev.flatlaf.icons.FlatAbstractIcon;

import javax.swing.*;
import java.awt.*;

public final class LoadingIcon extends FlatAbstractIcon {
    private static final int SEGMENTS = 8;
    private static final int WIDTH = 16;
    private static final int HEIGHT = 16;

    public LoadingIcon() {
        super(WIDTH, HEIGHT, UIManager.getColor("Objects.Grey"));
    }

    public static Timer createTimer(Component component) {
        return new Timer(1000 / SEGMENTS, _ -> component.repaint());
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g2) {
        int iteration = (int) (System.currentTimeMillis() / (1000 / SEGMENTS) % SEGMENTS);
        for (int i = 0; i < SEGMENTS; i++) {
            int ow = WIDTH / 4;
            int oh = HEIGHT / 4;
            int ox = ow + WIDTH / 8;
            int oy = oh + HEIGHT / 8;
            double angle = Math.PI * 2.0 * ((iteration + i) % SEGMENTS) / SEGMENTS;

            if (i > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) i / SEGMENTS));
            }

            g2.fillOval((int) (Math.cos(angle) * ox) + ox, (int) (Math.sin(angle) * oy) + oy, ow, oh);
        }
    }
}
