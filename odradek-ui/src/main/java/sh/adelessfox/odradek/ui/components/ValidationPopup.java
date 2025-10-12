package sh.adelessfox.odradek.ui.components;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;

public final class ValidationPopup {
    public enum Severity {
        INFO(null),
        WARNING(FlatClientProperties.OUTLINE_WARNING),
        ERROR(FlatClientProperties.OUTLINE_ERROR);

        private final String outline;

        Severity(String outline) {
            this.outline = outline;
        }
    }

    private final JComponent component;
    private final JToolTip toolTip;
    private Popup popup;
    private boolean visible;

    public ValidationPopup(JComponent component) {
        this.component = component;
        this.toolTip = new JToolTip();
        this.toolTip.putClientProperty(FlatClientProperties.POPUP_DROP_SHADOW_PAINTED, false);
        setSeverity(Severity.INFO);

        Handler handler = new Handler();
        component.addFocusListener(handler);
        component.addMouseListener(handler);
    }

    public void setMessage(String message) {
        Objects.requireNonNull(message, "message");
        toolTip.setTipText(message);
    }

    public void setSeverity(Severity severity) {
        Objects.requireNonNull(severity, "severity");
        toolTip.putClientProperty(FlatClientProperties.OUTLINE, severity.outline);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;

        if (visible) {
            show();
        } else {
            hide();
        }
    }

    private void show() {
        if (component == null || !component.isShowing() || !visible) {
            return;
        }

        if (popup != null) {
            hide();
        }

        Point location = component.getLocationOnScreen();
        location.x += 20;
        location.y -= 5 + (int) toolTip.getPreferredSize().getHeight();

        popup = PopupFactory.getSharedInstance().getPopup(component, toolTip, location.x, location.y);
        popup.show();
    }

    private void hide() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    private class Handler implements FocusListener, MouseListener {
        @Override
        public void focusGained(FocusEvent e) {
            show();
        }

        @Override
        public void focusLost(FocusEvent e) {
            hide();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (!component.hasFocus()) {
                show();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!component.hasFocus()) {
                hide();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // do nothing
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // do nothing
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // do nothing
        }
    }
}
