package sh.adelessfox.odradek.viewer.font;

import java.awt.*;

// https://tips4java.wordpress.com/2008/11/06/wrap-layout/
public class WrapLayout extends FlowLayout {
    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getWidth();
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            var insets = target.getInsets();
            int horizontalInsetsAndGaps = insets.left + insets.right + hgap * 2;
            int maxWidth = targetWidth - horizontalInsetsAndGaps;

            var dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int count = target.getComponentCount();
            for (int i = 0; i < count; i++) {
                var component = target.getComponent(i);

                if (component.isVisible()) {
                    var size = preferred ? component.getPreferredSize() : component.getMinimumSize();

                    if (rowWidth + size.width + hgap > maxWidth) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    if (rowWidth > 0) {
                        rowWidth += hgap;
                    }

                    rowWidth += size.width;
                    rowHeight = Math.max(rowHeight, size.height);
                }
            }

            addRow(dim, rowWidth, rowHeight);

            dim.width += horizontalInsetsAndGaps;
            dim.height += insets.top + insets.bottom + vgap * 2;

            return dim;
        }
    }

    private void addRow(Dimension dim, int rowWidth, int rowHeight) {
        if (rowWidth <= 0) {
            return;
        }
        dim.width = Math.max(dim.width, rowWidth);
        if (dim.height > 0) {
            dim.height += getVgap();
        }
        dim.height += rowHeight;
    }
}
