package sh.adelessfox.odradek.ui.components.tools.internal;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import sh.adelessfox.odradek.ui.components.tools.ToolPanel;

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
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);

            int arc = UIScale.scale(10);
            Color defaultColor = UIManager.getColor("ToolPanelButton.background");
            Color selectionColor = UIManager.getColor("ToolPanelButton.selectedBackground");
            Color focusedSelectedColor = UIManager.getColor("ToolPanelButton.focusedSelectedColor");
            Color rolloverColor = UIManager.getColor("ToolPanelButton.rolloverBackground");

            boolean isRollover = rollover;
            boolean isSelected = selected.test(provider);
            boolean isFocused = focused.test(provider);

            g2.setColor(isSelected ? isFocused ? focusedSelectedColor : selectionColor : isRollover ? rolloverColor : defaultColor);
            g2.fillRoundRect(4, 4, 24, 24, arc, arc);

            Icon icon = provider.icon();
            if (icon != null) {
                icon.paintIcon(this, g2, 16 - icon.getIconWidth() / 2, 16 - icon.getIconHeight() / 2);
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(32, 32);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    private class Handler extends MouseAdapter implements FocusListener {
        private final Consumer<ToolPanel.Provider> clicked;

        public Handler(Consumer<ToolPanel.Provider> clicked) {
            this.clicked = clicked;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            armed = true;
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
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
