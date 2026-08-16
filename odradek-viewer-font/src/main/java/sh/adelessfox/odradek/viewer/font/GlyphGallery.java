package sh.adelessfox.odradek.viewer.font;

import sh.adelessfox.odradek.font.Font;
import sh.adelessfox.odradek.font.Glyph;

import javax.swing.*;
import java.awt.*;

final class GlyphGallery extends JPanel implements Scrollable {
    GlyphGallery(Font font, int size) {
        setLayout(new WrapLayout(FlowLayout.LEFT, 0, 0));

        for (Glyph glyph : font.glyphs()) {
            add(new GlyphPanel(font, glyph, size));
        }
    }

    void setSize(int size) {
        for (Component comp : getComponents()) {
            if (comp instanceof GlyphPanel panel) {
                panel.setSize(size);
            }
        }
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
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
