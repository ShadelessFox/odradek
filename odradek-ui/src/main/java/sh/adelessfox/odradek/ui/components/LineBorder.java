package sh.adelessfox.odradek.ui.components;

import wtf.reversed.toolbox.util.Check;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

// Created due to https://github.com/JFormDesigner/FlatLaf/issues/1114.
// Remove once fixed or a workaround is found.
public final class LineBorder extends AbstractBorder {
    private static final LineBorder LINE_BORDER = new LineBorder(1, 1, 1, 1);

    private final int top;
    private final int left;
    private final int bottom;
    private final int right;

    private LineBorder(int top, int left, int bottom, int right) {
        this.top = Check.positiveOrZero(top, "top");
        this.left = Check.positiveOrZero(left, "left");
        this.bottom = Check.positiveOrZero(bottom, "bottom");
        this.right = Check.positiveOrZero(right, "right");
    }

    /**
     * Creates a line border with the default thickness of 1 pixel on all sides.
     *
     * @return a new line border with the default thickness
     */
    public static LineBorder of() {
        return LINE_BORDER;
    }

    /**
     * Creates a line border with the specified thickness per side.
     *
     * @param top    thickness of the top line
     * @param left   thickness of the left line
     * @param bottom thickness of the bottom line
     * @param right  thickness of the right line
     * @return a new line border with the specified thickness
     */
    public static LineBorder of(int top, int left, int bottom, int right) {
        return new LineBorder(top, left, bottom, right);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(UIManager.getColor("Component.borderColor"));
        if (top != 0) {
            g.fillRect(x, y, width, top);
        }
        if (left != 0) {
            g.fillRect(x, y + top, left, height - top - bottom);
        }
        if (bottom != 0) {
            g.fillRect(x, y + height - bottom, width, bottom);
        }
        if (right != 0) {
            g.fillRect(x + width - right, y + top, right, height - top - bottom);
        }
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.top = top;
        insets.left = left;
        insets.bottom = bottom;
        insets.right = right;
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}
