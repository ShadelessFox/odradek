package sh.adelessfox.odradek.ui.util;

import javax.swing.*;
import java.awt.*;

public final class GraphicsUtils {
    private GraphicsUtils() {
    }

    public static void drawCenteredString(Graphics g, String text, Component c) {
        drawCenteredString(g, text, c.getWidth(), c.getHeight());
    }

    public static void drawCenteredString(Graphics g, String text, int width, int height) {
        var fm = g.getFontMetrics();
        var lines = text.split("\n");

        int x = 0;
        int y = (height - fm.getHeight() * lines.length + 1) / 2 + fm.getAscent();
        for (String line : lines) {
            g.drawString(line, x + (width - fm.stringWidth(line)) / 2, y);
            y += fm.getHeight();
        }
    }

    public static void setTextRenderingHints(Graphics g) {
        var g2 = (Graphics2D) g;

        var hint1 = UIManager.get(RenderingHints.KEY_TEXT_ANTIALIASING);
        if (hint1 != null) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, hint1);
        }

        var hint2 = UIManager.get(RenderingHints.KEY_TEXT_LCD_CONTRAST);
        if (hint2 != null) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, hint2);
        }
    }
}
