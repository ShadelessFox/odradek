package sh.adelessfox.odradek.ui.components.view;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import sh.adelessfox.odradek.ui.util.Listeners;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class ViewButton extends JComponent {
    private final Listeners<ActionListener> actionListeners = new Listeners<>(ActionListener.class);

    private String text;
    private Icon icon;
    private boolean rollover;

    ViewButton(ViewGroup group, ViewInfo info) {
        Handler handler = new Handler();
        addMouseListener(handler);

        setText(text);
        setIcon(icon);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            FlatUIUtils.setRenderingHints(g2);

            int arc = UIScale.scale(10);
            Color defaultColor = UIManager.getColor("Button.default.hoverBackground");
            Color hoverColor = UIManager.getColor("Button.default.pressedBackground");
            Color accentColor = UIManager.getColor("Component.accentColor");

            g2.setColor(rollover ? hoverColor : defaultColor);
            g2.fillRoundRect(4, 4, 24, 24, arc, arc);

            Icon icon = getIcon();
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

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        String oldValue = this.text;
        this.text = text;
        firePropertyChange("text", oldValue, text);
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        Icon oldValue = this.icon;
        this.icon = icon;
        firePropertyChange("icon", oldValue, icon);
    }

    private class Handler extends MouseAdapter {
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
        public void mouseClicked(MouseEvent e) {
            ActionEvent event = new ActionEvent(
                this,
                ActionEvent.ACTION_PERFORMED,
                null,
                EventQueue.getMostRecentEventTime(),
                0
            );
            actionListeners.broadcast().actionPerformed(event);
        }
    }
}
