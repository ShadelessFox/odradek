package sh.adelessfox.odradek.ui.components.laf;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import sh.adelessfox.odradek.ui.components.FrameBackgroundPainter;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import java.awt.*;

/** Replaces FlatLaf's strip fill while retaining its layout and individual tab-state painting. */
public class FlatGradientTabbedPaneUI extends FlatTabbedPaneUI {
    private static final String ORIGINAL_OPAQUE = "Odradek.tabAreaOriginalOpaque";

    @SuppressWarnings("unused")
    public static ComponentUI createUI(JComponent component) {
        return new FlatGradientTabbedPaneUI();
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {
        if (!FrameBackgroundPainter.canPaint(tabPane) || hideTabArea() || tabPane.getTabCount() == 0) {
            updateHeaderWrappers(false);
            super.paint(graphics, component);
            return;
        }

        ensureCurrentLayout();

        var g = (Graphics2D) graphics.create();
        boolean painted;
        try {
            g.clip(tabStripBounds());
            painted = FrameBackgroundPainter.paint(g, tabPane);
        } finally {
            g.dispose();
        }

        updateHeaderWrappers(painted);

        if (!painted) {
            super.paint(graphics, component);
            return;
        }

        // Match FlatLaf's paint phases without its solid strip fill.
        // Do not change tabsOpaque: selected, hovered and custom tab backgrounds still need it
        int placement = tabPane.getTabPlacement();
        int selected = tabPane.getSelectedIndex();
        paintContentBorder(graphics, placement, selected);
        if (tabPane.getTabLayoutPolicy() != JTabbedPane.SCROLL_TAB_LAYOUT) {
            paintTabArea(graphics, placement, selected);
        }
    }

    @Override
    protected void paintTabArea(Graphics graphics, int placement, int selected) {
        if (!hideTabArea()
            && tabPane.getTabCount() > 0
            && tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT
            && tabViewport != null
            && tabViewport.getView() instanceof JComponent view
        ) {
            // This callback's graphics are in the scrolling tab view's coordinates, not the JTabbedPane's.
            // Include the view position to avoid a moving gradient
            FrameBackgroundPainter.paint(graphics, view);
        }
        super.paintTabArea(graphics, placement, selected);
    }

    private Rectangle tabStripBounds() {
        var insets = tabPane.getInsets();
        var bounds = new Rectangle(
            insets.left, insets.top,
            Math.max(0, tabPane.getWidth() - insets.left - insets.right),
            Math.max(0, tabPane.getHeight() - insets.top - insets.bottom));
        int placement = tabPane.getTabPlacement();
        if (placement == TOP || placement == BOTTOM) {
            int height = Math.clamp(calculateTabAreaHeight(placement, runCount, maxTabHeight), 0, bounds.height);
            if (placement == BOTTOM) {
                bounds.y += bounds.height - height;
            }
            bounds.height = height;
        } else {
            int width = Math.clamp(calculateTabAreaWidth(placement, runCount, maxTabWidth), 0, bounds.width);
            if (placement == RIGHT) {
                bounds.x += bounds.width - width;
            }
            bounds.width = width;
        }
        return bounds;
    }

    private void updateHeaderWrappers(boolean gradient) {
        updateHeaderWrapper(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, gradient);
        updateHeaderWrapper(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, gradient);
    }

    private void updateHeaderWrapper(String property, boolean gradient) {
        if (tabPane.getClientProperty(property) instanceof Component component
            && component.getParent() instanceof JComponent wrapper
            && wrapper instanceof UIResource && wrapper.getParent() == tabPane
        ) {
            // Only change FlatLaf's private wrapper!
            var original = (Boolean) wrapper.getClientProperty(ORIGINAL_OPAQUE);
            if (original == null) {
                original = wrapper.isOpaque();
                wrapper.putClientProperty(ORIGINAL_OPAQUE, original);
            }
            wrapper.setOpaque(!gradient && original);
        }
    }
}
