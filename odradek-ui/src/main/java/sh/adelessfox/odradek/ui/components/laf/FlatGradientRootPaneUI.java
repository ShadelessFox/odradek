package sh.adelessfox.odradek.ui.components.laf;

import com.formdev.flatlaf.ui.FlatRootPaneUI;
import com.formdev.flatlaf.ui.FlatTitlePane;
import sh.adelessfox.odradek.ui.components.FrameBackgroundPainter;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public final class FlatGradientRootPaneUI extends FlatRootPaneUI {
    private final ComponentListener repaintOnResize = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent event) {
            var root = (JRootPane) event.getComponent();
            if (FrameBackgroundPainter.canPaint(root)) {
                // Height changes also affect the gradient in the fixed-height title/menu bar
                root.repaint();
            }
        }
    };

    @SuppressWarnings("unused")
    public static ComponentUI createUI(JComponent component) {
        return new FlatGradientRootPaneUI();
    }

    @Override
    protected void installListeners(JRootPane root) {
        super.installListeners(root);
        root.addComponentListener(repaintOnResize);
    }

    @Override
    protected void uninstallListeners(JRootPane root) {
        root.removeComponentListener(repaintOnResize);
        super.uninstallListeners(root);
    }

    @Override
    public void update(Graphics graphics, JComponent component) {
        if (FrameBackgroundPainter.paint(graphics, component)) {
            paint(graphics, component);
        } else {
            super.update(graphics, component);
        }
    }

    @Override
    protected FlatTitlePane createTitlePane() {
        return new FlatTitlePane(rootPane) {
            @Override
            protected void paintComponent(Graphics graphics) {
                if (!FrameBackgroundPainter.canPaint(this)) {
                    super.paintComponent(graphics);
                }
            }
        };
    }
}
