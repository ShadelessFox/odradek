package sh.adelessfox.odradek.app.ui.laf;

import com.formdev.flatlaf.ui.FlatBorder;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.awt.*;

public class FlatSplitPaneBorder extends FlatBorder {
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        var pane = ((BasicSplitPaneDivider) c).getBasicSplitPaneUI().getSplitPane();
        var vertical = pane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;

        g.setColor(UIManager.getColor("Component.borderColor"));

        if (pane.getLeftComponent() != null) {
            if (vertical) {
                g.fillRect(x, y, 1, height);
            } else {
                g.fillRect(x, y, width, 1);
            }
        }

        if (pane.getRightComponent() != null) {
            if (vertical) {
                g.fillRect(x + width - 1, y, 1, height);
            } else {
                g.fillRect(x, y + height - 1, width, 1);
            }
        }
    }
}
