package sh.adelessfox.odradek.ui.components.toolwindow;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

final class ToolWindowButton extends JComponent {
    private final ToolWindowGroup group;
    private final ToolWindowInfo info;
    private final Icon icon;

    private boolean rollover;
    private boolean armed;

    ToolWindowButton(ToolWindowGroup group, ToolWindowInfo info, Icon icon, Runnable clicked) {
        this.group = group;
        this.info = info;
        this.icon = icon;

        Handler handler = new Handler(clicked);
        addMouseListener(handler);
        addFocusListener(handler);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);

            int arc = UIScale.scale(10);
            Color defaultColor = UIManager.getColor("Button.default.hoverBackground");
            Color rolloverColor = UIManager.getColor("Button.default.pressedBackground");
            Color selectionColor = UIManager.getColor("Button.selectedBackground");
            Color focusSelectionColor = UIManager.getColor("Button.default.borderColor");

            boolean isRollover = rollover;
            boolean isSelected = group.isSelected(info);
            boolean isFocused = isButtonOrChildFocused();

            g2.setColor(isSelected ? (isFocused ? focusSelectionColor : selectionColor) : isRollover ? rolloverColor : defaultColor);
            g2.fillRoundRect(4, 4, 24, 24, arc, arc);

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

    private boolean isButtonOrChildFocused() {
        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        Component focusOwner = keyboardFocusManager.getPermanentFocusOwner();
        return focusOwner != null
            && SwingUtilities.isDescendingFrom(focusOwner, group.getComponent())
            && isInActiveWindow(focusOwner, keyboardFocusManager.getActiveWindow());
    }

    static boolean isInActiveWindow(Component c, Window activeWindow) {
        Window window = SwingUtilities.windowForComponent(c);
        return window == activeWindow
            || window != null && window.getType() == Window.Type.POPUP && window.getOwner() == activeWindow;
    }

    private class Handler extends MouseAdapter implements FocusListener {
        private final Runnable clicked;

        public Handler(Runnable clicked) {
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
                clicked.run();
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
