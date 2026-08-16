package sh.adelessfox.odradek.viewer.font;

import com.formdev.flatlaf.ui.FlatUIUtils;
import sh.adelessfox.odradek.font.Font;
import sh.adelessfox.odradek.font.Glyph;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

final class GlyphPanel extends JComponent {
    private final Font font;
    private final Glyph glyph;
    private final Path2D.Float path;
    private int size;

    GlyphPanel(Font font, Glyph glyph, int size) {
        this.font = font;
        this.glyph = glyph;
        this.path = glyph.toPath();
        this.size = size;

        ToolTipManager.sharedInstance().registerComponent(this);
    }

    void setSize(int size) {
        if (this.size != size) {
            this.size = size;
            revalidate();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);
            doPaint(g2);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        return new Dimension(size, size);
    }

    @Override
    public Dimension getMinimumSize() {
        if (isMinimumSizeSet()) {
            return super.getMinimumSize();
        }
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        if (isMaximumSizeSet()) {
            return super.getMaximumSize();
        }
        return getPreferredSize();
    }

    @Override
    public String getToolTipText() {
        if (!Character.isValidCodePoint(glyph.codePoint())) {
            return null;
        }
        return "U+%04X %s".formatted(glyph.codePoint(), Character.getName(glyph.codePoint()));
    }

    private void doPaint(Graphics2D g) {
        int width = getWidth();
        int height = getHeight();

        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);

        g.setColor(UIManager.getColor("Component.borderColor"));
        g.drawRect(-1, -1, width, height);

        float textWidth = glyph.metrics().advanceWidth();
        float textHeight = font.metrics().height();
        float scale = size / textHeight;

        g.translate((width - textWidth * scale) / 2, (height - textHeight * scale) / 2);
        g.scale(scale, -scale);
        g.translate(0, -textHeight);
        g.translate(0, font.metrics().descent());
        g.setColor(getForeground());
        g.fill(path);
    }
}
