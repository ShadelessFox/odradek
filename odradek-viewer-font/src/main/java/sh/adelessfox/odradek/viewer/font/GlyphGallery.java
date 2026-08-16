package sh.adelessfox.odradek.viewer.font;

import sh.adelessfox.odradek.font.Font;
import sh.adelessfox.odradek.font.Glyph;

import javax.swing.*;
import java.awt.*;

final class GlyphGallery extends JPanel implements Scrollable {
    GlyphGallery(Font font) {
        setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));

        for (Glyph glyph : font.glyphs()) {
            add(new GlyphPanel(font, glyph, 72));
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
