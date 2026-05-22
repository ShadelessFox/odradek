package sh.adelessfox.odradek.ui.components;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class SplitButton extends JButton {
    private final PopupMenuListener popupMenuListener = createPopupMenuListener();
    private JPopupMenu popupMenu;

    public SplitButton() {
        setHorizontalAlignment(SwingConstants.LEFT);
        setPopupMenu(new JPopupMenu());
        addActionListener(_ -> showPopupMenu());
    }

    @Override
    public String getUIClassID() {
        return "SplitButtonUI";
    }

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    public void setPopupMenu(JPopupMenu popupMenu) {
        if (this.popupMenu != popupMenu) {
            if (this.popupMenu != null) {
                this.popupMenu.removePopupMenuListener(popupMenuListener);
            }
            this.popupMenu = popupMenu;
            popupMenu.addPopupMenuListener(popupMenuListener);
        }
    }

    private void showPopupMenu() {
        popupMenu.show(this, 0, getHeight());
    }

    private PopupMenuListener createPopupMenuListener() {
        return new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                getModel().setPressed(true);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                getModel().setPressed(false);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                getModel().setPressed(false);
            }
        };
    }
}
