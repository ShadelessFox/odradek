package sh.adelessfox.odradek.ui.components.tools;

import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class ToolButton extends JComponent {
    private final ToolPanel.Provider provider;
    private final Predicate<ToolPanel.Provider> selected;
    private final Predicate<ToolPanel.Provider> focused;

    // Styles
    private Dimension size;
    private int arc;
    private Color defaultColor;
    private Color selectionColor;
    private Color focusedSelectedColor;
    private Color rolloverColor;

    private boolean rollover;
    private boolean armed;

    ToolButton(
        ToolPanel.Provider provider,
        Predicate<ToolPanel.Provider> selected,
        Predicate<ToolPanel.Provider> focused,
        Consumer<ToolPanel.Provider> clicked
    ) {
        this.provider = provider;
        this.selected = selected;
        this.focused = focused;

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

        size = UIManager.getDimension("ToolButton.size");
        arc = UIManager.getInt("ToolButton.arc");
        defaultColor = UIManager.getColor("ToolButton.background");
        selectionColor = UIManager.getColor("ToolButton.selectedBackground");
        focusedSelectedColor = UIManager.getColor("ToolButton.focusedSelectedColor");
        rolloverColor = UIManager.getColor("ToolButton.rolloverBackground");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);

            g2.setColor(getColor());
            g2.fillRoundRect(0, 0, size.width, size.height, arc, arc);

            var icon = provider.icon();
            if (icon != null) {
                icon.paintIcon(
                    this,
                    g2,
                    (size.width - icon.getIconWidth()) / 2,
                    (size.height - icon.getIconHeight()) / 2);
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private Color getColor() {
        boolean isRollover = rollover;
        boolean isSelected = selected.test(provider);
        boolean isFocused = focused.test(provider);

        if (isSelected) {
            return isFocused ? focusedSelectedColor : selectionColor;
        } else if (isRollover) {
            return armed ? selectionColor : rolloverColor;
        } else {
            return defaultColor;
        }
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
