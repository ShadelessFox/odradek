package sh.adelessfox.odradek.ui.components;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

// Created due to https://github.com/JFormDesigner/FlatLaf/issues/1114.
// Remove once fixed or a workaround is found.
public final class LineBorder extends AbstractBorder {
    private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
    private static final Insets DEFAULT_INSETS = new Insets(1, 1, 1, 1);

    private static final LineBorder SINGLE = new LineBorder(EMPTY_INSETS, DEFAULT_INSETS);

    private final Insets insets;
    private final Insets thickness;

    private LineBorder(Insets insets, Insets thickness) {
        this.insets = insets;
        this.thickness = thickness;
    }

    /**
     * Creates a line border with the default thickness of 1 pixel on all sides.
     */
    public static LineBorder of() {
        return SINGLE;
    }

    /**
     * Creates a line border with the specified thickness on all sides.
     *
     * @param top    thickness of the top border
     * @param left   thickness of the left border
     * @param bottom thickness of the bottom border
     * @param right  thickness of the right border
     * @return a new LineBorder instance with the specified thickness
     */
    public LineBorder withThickness(int top, int left, int bottom, int right) {
        return new LineBorder(insets, new Insets(top, left, bottom, right));
    }

    /**
     * Creates a line border with the specified insets on all sides.
     * <p>
     * The insets define the space between the border and the component's content.
     *
     * @param top    the space between the top border and the component's content
     * @param left   the space between the left border and the component's content
     * @param bottom the space between the bottom border and the component's content
     * @param right  the space between the right border and the component's content
     * @return a new LineBorder instance with the specified insets
     */
    public LineBorder withInsets(int top, int left, int bottom, int right) {
        return new LineBorder(new Insets(top, left, bottom, right), thickness);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(UIManager.getColor("Component.borderColor"));
        if (thickness.top != 0) {
            g.fillRect(x, y, width, thickness.top);
        }
        if (thickness.left != 0) {
            g.fillRect(x, y + thickness.top, thickness.left, height - thickness.top - thickness.bottom);
        }
        if (thickness.bottom != 0) {
            g.fillRect(x, y + height - thickness.bottom, width, thickness.bottom);
        }
        if (thickness.right != 0) {
            g.fillRect(x + width - thickness.right, y + thickness.top, thickness.right, height - thickness.top - thickness.bottom);
        }
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = this.insets.left;
        insets.top = this.insets.top;
        insets.right = this.insets.right;
        insets.bottom = this.insets.bottom;
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}
