package sh.adelessfox.odradek.ui.components.laf;

import com.formdev.flatlaf.ui.FlatButtonUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

public final class FlatSplitButtonUI extends FlatButtonUI {
    private Color arrowColor;
    private int arrowWidth;
    private int arrowGap;

    private boolean defaultsInitialized = false;

    private FlatSplitButtonUI(boolean shared) {
        super(shared);
    }

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c)
            ? FlatUIUtils.createSharedUI(FlatSplitButtonUI.class, () -> new FlatSplitButtonUI(true))
            : new FlatSplitButtonUI(false);
    }

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);

        if (!defaultsInitialized) {
            arrowColor = UIManager.getColor("SplitButton.arrowColor");
            arrowWidth = UIManager.getInt("SplitButton.arrowWidth");
            arrowGap = UIManager.getInt("SplitButton.arrowGap");
            defaultsInitialized = true;
        }
    }

    @Override
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        defaultsInitialized = false;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        paintArrow(g, c);
    }

    private void paintArrow(Graphics g, JComponent c) {
        int x = c.getWidth() - c.getInsets().right - UIScale.scale(Math.ceilDiv(arrowWidth, 2));
        int y = c.getHeight() / 2;

        var g2 = (Graphics2D) g.create();
        FlatUIUtils.setRenderingHints(g2);
        g2.setColor(arrowColor);
        FlatUIUtils.paintArrow(g2, x, y, 0, 0, SwingConstants.SOUTH, true, arrowWidth, 1, 0, 0);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = super.getPreferredSize(c);
        size.width += UIScale.scale(arrowWidth + arrowGap);
        return size;
    }
}
