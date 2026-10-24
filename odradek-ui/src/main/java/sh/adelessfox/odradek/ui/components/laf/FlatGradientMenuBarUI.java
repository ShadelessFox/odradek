package sh.adelessfox.odradek.ui.components.laf;

import com.formdev.flatlaf.ui.FlatMenuBarUI;
import sh.adelessfox.odradek.ui.components.FrameBackgroundPainter;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

public final class FlatGradientMenuBarUI extends FlatMenuBarUI {
    @SuppressWarnings("unused")
    public static ComponentUI createUI(JComponent component) {
        return new FlatGradientMenuBarUI();
    }

    @Override
    public void update(Graphics graphics, JComponent component) {
        var root = SwingUtilities.getRootPane(component);
        if (root != null && root.getJMenuBar() == component && FrameBackgroundPainter.canPaint(root)) {
            paint(graphics, component);
        } else {
            super.update(graphics, component);
        }
    }
}
