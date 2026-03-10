package sh.adelessfox.odradek.ui.components.tool;

import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

final class ToolPanelButton extends JComponent {
    private final ToolPanelGroup group;
    private final ToolPanel pane;
    private final Icon icon;

    // Styles
    private Dimension size;
    private Insets insets;
    private int arc;
    private Color defaultColor;
    private Color selectionColor;
    private Color focusedSelectedColor;
    private Color rolloverColor;

    private boolean rollover;
    private boolean armed;

    ToolPanelButton(ToolPanelGroup group, ToolPanel pane, Icon icon, Runnable clicked) {
        this.group = group;
        this.pane = pane;
        this.icon = icon;

        Handler handler = new Handler(clicked);
        addMouseListener(handler);
        addFocusListener(handler);
        setFocusable(true);

        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();

        size = UIManager.getDimension("ToolPanelButton.size");
        insets = UIManager.getInsets("ToolPanelButton.margin");
        arc = UIManager.getInt("ToolPanelButton.arc");
        defaultColor = UIManager.getColor("ToolPanelButton.background");
        selectionColor = UIManager.getColor("ToolPanelButton.selectedBackground");
        focusedSelectedColor = UIManager.getColor("ToolPanelButton.focusedSelectedColor");
        rolloverColor = UIManager.getColor("ToolPanelButton.rolloverBackground");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);

            g2.setColor(getColor());
            g2.fillRoundRect(
                insets.left,
                insets.top,
                size.width - insets.left - insets.right,
                size.height - insets.top - insets.bottom,
                arc,
                arc);

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
        boolean isSelected = group.isSelected(pane);
        if (isSelected) {
            boolean isFocused = isButtonOrChildFocused();
            return isFocused ? focusedSelectedColor : selectionColor;
        } else {
            boolean isRollover = rollover;
            return isRollover ? rolloverColor : defaultColor;
        }
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
