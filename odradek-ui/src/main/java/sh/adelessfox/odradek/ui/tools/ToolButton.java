package sh.adelessfox.odradek.ui.tools;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class ToolButton extends JComponent {
    private final ToolPanel.Provider provider;
    private final Predicate<ToolPanel.Provider> selected;
    private final Predicate<ToolPanel.Provider> focused;

    enum Placement {
        LEFT,
        RIGHT
    }

    enum Style {
        ICON,
        ICON_WITH_TEXT
    }

    private final Placement placement;
    private final Style style;

    // Styles
    private Insets insets;
    private int arc;
    private int textIconGap;

    private Color background;
    private Color hoverBackground;
    private Color selectedBackground;
    private Color focusedBackground;
    private Color borderColor;
    private Color selectedBorderColor;
    private Color focusedBorderColor;

    private boolean rollover;
    private boolean armed;

    ToolButton(
        ToolPanel.Provider provider,
        Predicate<ToolPanel.Provider> selected,
        Predicate<ToolPanel.Provider> focused,
        Consumer<ToolPanel.Provider> clicked,
        Placement placement,
        Style style
    ) {
        this.provider = provider;
        this.selected = selected;
        this.focused = focused;
        this.placement = placement;
        this.style = style;

        var handler = new Handler(clicked);
        addMouseListener(handler);
        addFocusListener(handler);

        setToolTipText(provider.name());
        setFocusable(false);

        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();

        insets = UIManager.getInsets("ToolButton.margin");
        textIconGap = UIManager.getInt("ToolButton.textIconGap");
        arc = UIManager.getInt("ToolButton.arc");

        background = UIManager.getColor("ToolButton.background");
        hoverBackground = UIManager.getColor("ToolButton.hoverBackground");
        selectedBackground = UIManager.getColor("ToolButton.selectedBackground");
        focusedBackground = UIManager.getColor("ToolButton.focusedBackground");

        borderColor = UIManager.getColor("ToolButton.borderColor");
        selectedBorderColor = UIManager.getColor("ToolButton.selectedBorderColor");
        focusedBorderColor = UIManager.getColor("ToolButton.focusedBorderColor");
    }

    @Override
    @SuppressWarnings("SuspiciousNameCombination")
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);

            boolean isSelected = selected.test(provider);
            boolean isFocused = focused.test(provider);

            g2.setColor(computeBackgroundColor(isSelected, isFocused));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2.setColor(computeBorderColor(isSelected, isFocused));
            g2.setStroke(new BasicStroke(UIScale.scale(1)));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

            var viewR = new Rectangle(
                insets.top,
                insets.left,
                getHeight() - insets.top - insets.bottom,
                getWidth() - insets.left - insets.right);
            var iconR = new Rectangle();
            var textR = new Rectangle();

            var fm = getFontMetrics(getFont());
            var text = layoutCL(fm, viewR, iconR, textR);

            var icon = provider.icon();
            if (icon != null) {
                if (style == Style.ICON_WITH_TEXT) {
                    var oldTransform = rotateGraphics(g2, placement, iconR);
                    icon.paintIcon(this, g2, iconR.y, iconR.x);
                    g2.setTransform(oldTransform);
                } else {
                    icon.paintIcon(this, g2, iconR.y, iconR.x);
                }
            }

            if (style == Style.ICON_WITH_TEXT) {
                rotateGraphics(g2, placement, textR);
                g2.setColor(getForeground());
                g2.drawString(text, textR.y, textR.x + fm.getAscent());
            }
        } finally {
            g2.dispose();
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private AffineTransform rotateGraphics(Graphics2D g2, Placement placement, Rectangle r) {
        var oldTransform = g2.getTransform();

        if (placement == Placement.LEFT) {
            g2.translate(0, r.width);
            g2.rotate(Math.toRadians(270), r.y, r.x);
        } else {
            g2.translate(r.height, 0);
            g2.rotate(Math.toRadians(90), r.y, r.x);
        }

        return oldTransform;
    }

    @Override
    public Dimension getPreferredSize() {
        int dx = insets.left + insets.right;
        int dy = insets.top + insets.bottom;

        var viewR = new Rectangle(dx, dy, Short.MAX_VALUE, Short.MAX_VALUE);
        var iconR = new Rectangle();
        var textR = new Rectangle();
        layoutCL(getFontMetrics(getFont()), viewR, iconR, textR);

        int x1 = Math.min(iconR.x, textR.x);
        int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
        int y1 = Math.min(iconR.y, textR.y);
        int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);

        return new Dimension(
            y2 - y1 + dy,
            x2 - x1 + dx);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private Color computeBackgroundColor(boolean selected, boolean focused) {
        if (selected) {
            return focused ? focusedBackground : selectedBackground;
        } else if (rollover) {
            return armed ? selectedBackground : hoverBackground;
        } else {
            return background;
        }
    }

    private Color computeBorderColor(boolean selected, boolean focused) {
        if (selected) {
            return focused ? focusedBorderColor : selectedBorderColor;
        } else {
            return borderColor;
        }
    }

    private String layoutCL(FontMetrics fm, Rectangle viewR, Rectangle iconR, Rectangle textR) {
        return SwingUtilities.layoutCompoundLabel(
            fm,
            style == Style.ICON_WITH_TEXT ? provider.name() : null,
            provider.icon(),
            SwingConstants.CENTER,
            SwingConstants.LEADING,
            SwingConstants.CENTER,
            SwingConstants.TRAILING,
            viewR,
            iconR,
            textR,
            textIconGap);
    }

    private class Handler extends MouseAdapter implements FocusListener {
        private final Consumer<ToolPanel.Provider> clicked;

        public Handler(Consumer<ToolPanel.Provider> clicked) {
            this.clicked = clicked;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                armed = true;
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
            if (armed && rollover) {
                clicked.accept(provider);
            }
            armed = false;
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            rollover = true;
            repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            rollover = false;
            repaint();
        }

        @Override
        public void focusGained(FocusEvent e) {
            repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            repaint();
        }
    }
}
