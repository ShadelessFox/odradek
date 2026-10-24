package sh.adelessfox.odradek.ui.components.laf;

import com.formdev.flatlaf.ui.FlatPopupMenuUI;
import sh.adelessfox.odradek.ui.components.FrameBackgroundPainter;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;

public final class FlatGradientPopupMenuUI extends FlatPopupMenuUI {
    @SuppressWarnings("unused")
    public static ComponentUI createUI(JComponent component) {
        return new FlatGradientPopupMenuUI();
    }

    @Override
    public void update(Graphics graphics, JComponent component) {
        if (component instanceof JPopupMenu popup && !(popup instanceof BasicComboPopup) && FrameBackgroundPainter.paintPopup(graphics, popup)) {
            paint(graphics, component);
        } else {
            super.update(graphics, component);
        }
    }
}
