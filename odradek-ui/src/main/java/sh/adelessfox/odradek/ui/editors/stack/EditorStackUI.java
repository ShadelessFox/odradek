package sh.adelessfox.odradek.ui.editors.stack;

import com.formdev.flatlaf.ui.FlatTabbedPaneUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

public final class EditorStackUI extends FlatTabbedPaneUI {
    private Color focusBorderColor;

    @SuppressWarnings("unused")
    public static ComponentUI createUI(JComponent c) {
        return new EditorStackUI();
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        focusBorderColor = UIManager.getColor("EditorStack.focusBorderColor");
    }

    @Override
    protected void uninstallDefaults() {
        super.uninstallDefaults();
        focusBorderColor = null;
    }

    @Override
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        withFocusColor(() -> super.paintTabArea(g, tabPlacement, selectedIndex));
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        withFocusColor(() -> super.paintContentBorder(g, tabPlacement, selectedIndex));
    }

    private void withFocusColor(Runnable action) {
        if (!isTabbedPaneOrChildFocused()) {
            action.run();
            return;
        }
        var oldContentAreaColor = contentAreaColor;
        var oldTabSeparatorColor = tabSeparatorColor;
        try {
            contentAreaColor = focusBorderColor;
            tabSeparatorColor = focusBorderColor;
            action.run();
        } finally {
            contentAreaColor = oldContentAreaColor;
            tabSeparatorColor = oldTabSeparatorColor;
        }
    }
}
