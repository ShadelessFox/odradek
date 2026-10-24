package sh.adelessfox.odradek.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.IdentityHashMap;

/** Paints frame-aligned backgrounds; menu popups use their invoker's root coordinates. */
public final class FrameBackgroundPainter {
    private static final String ENABLED_PROPERTY = "Odradek.frameBackground";

    private FrameBackgroundPainter() {
    }

    /** Changes this window's preference and repaints affected surfaces. Call on the EDT. */
    public static void setEnabled(JRootPane root, boolean enabled) {
        root.putClientProperty(ENABLED_PROPERTY, enabled);
        root.setOpaque(true);
        root.repaint();

        var window = SwingUtilities.getWindowAncestor(root);
        if (window != null) {
            repaintPopups(window);
        }
    }

    /** Whether the window is opted in and the current theme supplies both gradient colors. */
    public static boolean canPaint(JComponent component) {
        var root = rootPane(component);
        return root != null && Boolean.TRUE.equals(root.getClientProperty(ENABLED_PROPERTY))
            && UIManager.getColor("Frame.backgroundStart") != null
            && UIManager.getColor("Frame.backgroundEnd") != null;
    }

    public static boolean paint(Graphics graphics, JComponent component) {
        return paint(graphics, component, rootPane(component));
    }

    public static boolean paintPopup(Graphics graphics, JPopupMenu popup) {
        var root = popupRoot(popup);
        if (root == null || (!SwingUtilities.isDescendingFrom(popup, root) && (!popup.isShowing() || !root.isShowing()))) {
            // A detached popup has no meaningful screen position yet
            return false;
        }
        return paint(graphics, popup, root);
    }

    private static boolean paint(Graphics graphics, JComponent component, JRootPane root) {
        if (root == null || !canPaint(root)) {
            return false;
        }

        var start = UIManager.getColor("Frame.backgroundStart");
        var end = UIManager.getColor("Frame.backgroundEnd");
        var offset = SwingUtilities.convertPoint(component, 0, 0, root);

        var g = (Graphics2D) graphics.create();
        try {
            g.translate(-offset.x, -offset.y);
            g.setPaint(new GradientPaint(
                0,
                0,
                start,
                Math.max(1, root.getWidth()),
                Math.max(1, root.getHeight()),
                end));
            g.fillRect(offset.x, offset.y, component.getWidth(), component.getHeight());
        } finally {
            g.dispose();
        }

        return true;
    }

    private static JRootPane rootPane(Component component) {
        return component instanceof JRootPane root ? root : SwingUtilities.getRootPane(component);
    }

    private static void repaintPopups(Window owner) {
        for (var window : owner.getOwnedWindows()) {
            if (window.getType() == Window.Type.POPUP && window.isShowing()) {
                window.repaint();
                repaintPopups(window);
            }
        }
    }

    private static JRootPane popupRoot(JPopupMenu popup) {
        var visited = Collections.newSetFromMap(new IdentityHashMap<Component, Boolean>());
        var current = (Component) popup;
        while (current != null && visited.add(current)) {
            if (current instanceof JPopupMenu menu) {
                current = menu.getInvoker();
            } else {
                var parentMenu = SwingUtilities.getAncestorOfClass(JPopupMenu.class, current);
                if (parentMenu == null) {
                    return rootPane(current);
                }
                current = parentMenu;
            }
        }
        return null;
    }
}
