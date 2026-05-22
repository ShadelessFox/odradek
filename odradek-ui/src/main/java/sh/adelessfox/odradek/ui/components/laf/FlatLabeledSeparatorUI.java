package sh.adelessfox.odradek.ui.components.laf;

import com.formdev.flatlaf.ui.FlatSeparatorUI;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import sh.adelessfox.odradek.ui.components.LabeledSeparator;
import sh.adelessfox.odradek.ui.util.GraphicsUtils;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public final class FlatLabeledSeparatorUI extends FlatSeparatorUI {
    private Color labelForeground;
    private boolean defaultsInitialized = false;

    private FlatLabeledSeparatorUI(boolean shared) {
        super(shared);
    }

    public static ComponentUI createUI(JComponent c) {
        return FlatUIUtils.canUseSharedUI(c)
            ? FlatUIUtils.createSharedUI(FlatLabeledSeparatorUI.class, () -> new FlatLabeledSeparatorUI(true))
            : new FlatLabeledSeparatorUI(false);
    }

    @Override
    protected void installDefaults(JSeparator s) {
        super.installDefaults(s);

        if (!defaultsInitialized) {
            labelForeground = UIManager.getColor("LabeledSeparator.labelForeground");
            defaultsInitialized = true;
        }
    }

    @Override
    protected void uninstallDefaults(JSeparator s) {
        super.uninstallDefaults(s);
        defaultsInitialized = false;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        var g2 = (Graphics2D) g.create();
        try {
            String label = ((LabeledSeparator) c).getLabel();
            float width = UIScale.scale((float) stripeWidth);
            float indent = UIScale.scale((float) stripeIndent);
            float height = getHeight(c);
            float shift;

            if (label != null && !label.isEmpty()) {
                var metrics = c.getFontMetrics(g.getFont());

                GraphicsUtils.setTextRenderingHints(g2);
                g2.setColor(labelForeground);
                g2.drawString(label, indent, metrics.getAscent());

                shift = metrics.stringWidth(label) + UIScale.scale(5f);
            } else {
                shift = 0;
            }

            FlatUIUtils.setRenderingHints(g2);
            g2.setColor(c.getForeground());
            g2.fill(new Rectangle2D.Float(shift, indent + height / 2, c.getWidth() - shift, width));
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return new Dimension(0, getHeight(c));
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    private int getHeight(JComponent c) {
        var label = ((LabeledSeparator) c).getLabel();
        if (label != null && !label.isEmpty()) {
            return c.getFontMetrics(c.getFont()).getHeight();
        } else {
            return UIScale.scale(this.height);
        }
    }
}
