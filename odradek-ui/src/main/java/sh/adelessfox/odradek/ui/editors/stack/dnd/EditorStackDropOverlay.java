package sh.adelessfox.odradek.ui.editors.stack.dnd;

import com.formdev.flatlaf.ui.FlatUIUtils;
import sh.adelessfox.odradek.ui.editors.stack.EditorStack;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.Optional;

/**
 * Overlay window used for editor stack drag-and-drop operations.
 * <p>
 * It appears when an editor tab is being dragged and provides visual feedback
 * for potential drop targets within the editor stack.
 * <p>
 * After the drag operation completes, the overlay is hidden.
 */
public final class EditorStackDropOverlay extends JComponent {
    private static final int TAB_OVERLAY_WIDTH = 50;
    private static final int TAB_OVERLAY_PADDING = 2;
    private static final int TAB_OVERLAY_ARC = 10;

    private final EditorStackManager manager;
    private EditorStackDropTarget target;

    private Color overlayBackgroundColor;
    private Color overlayBorderColor;

    public EditorStackDropOverlay(EditorStackManager manager) throws HeadlessException {
        this.manager = manager;

        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setBackground(new Color(0, 0, 0, 0));

        overlayBackgroundColor = UIManager.getColor("EditorStack.overlay.background");
        overlayBorderColor = UIManager.getColor("EditorStack.overlay.borderColor");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        FlatUIUtils.setRenderingHints(g2);

        switch (target) {
            case EditorStackDropTarget.Move(var stack, int index) -> paintMoveMarker(g2, stack, index);
            case EditorStackDropTarget.Split(var stack, var position) -> paintSplitMarker(g2, stack, position);
            case null -> {
                // nothing to do
            }
        }

        g2.dispose();
    }

    /**
     * Updates the overlay to reflect the current drop target based on the given screen coordinates.
     *
     * @param sourceStack The stack from which the drag operation originated
     * @param screenX     The current screen X coordinate of the mouse
     * @param screenY     The current screen Y coordinate of the mouse
     */
    public void update(EditorStack sourceStack, int screenX, int screenY) {
        var newTarget = determineDropTarget(manager.getRoot(), sourceStack, screenX, screenY);
        if (!Objects.equals(target, newTarget)) {
            target = newTarget;
            repaint();
        }
    }

    /**
     * Cleans up resources used by the overlay.
     */
    public void release() {
        target = null;
    }

    /**
     * Gets the current drop target, if any.
     */
    public Optional<EditorStackDropTarget> getTarget() {
        return Optional.ofNullable(target);
    }

    private void paintMoveMarker(Graphics2D g2, EditorStack stack, int index) {
        Point origin = stack.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(origin, this);

        Rectangle rect;
        if (index == 0) { // first
            var bounds = stack.getBoundsAt(index);
            rect = new Rectangle(
                origin.x + bounds.x,
                origin.y + bounds.y,
                bounds.width / 2,
                bounds.height);
        } else if (index == stack.getTabCount()) { // last
            var bounds = stack.getBoundsAt(index - 1);
            rect = new Rectangle(
                origin.x + bounds.x + bounds.width - TAB_OVERLAY_WIDTH / 2,
                origin.y + bounds.y,
                TAB_OVERLAY_WIDTH,
                bounds.height);
        } else {
            var bounds = stack.getBoundsAt(index);
            rect = new Rectangle(
                origin.x + bounds.x - TAB_OVERLAY_WIDTH / 2,
                origin.y + bounds.y,
                50,
                bounds.height);
        }

        g2.setColor(overlayBackgroundColor);
        g2.fill(getTabVisualShape(rect, true));

        g2.setColor(overlayBorderColor);
        g2.draw(getTabVisualShape(rect, false));
    }

    private void paintSplitMarker(Graphics2D g2, EditorStack stack, EditorStack.Position position) {
        Point origin = stack.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(origin, this);

        Rectangle bounds = stack.getComponentAt(0).getBounds();
        bounds.x += origin.x;
        bounds.y += origin.y;

        g2.setColor(overlayBackgroundColor);
        g2.fill(getSplitVisualShape(bounds, position, true));

        g2.setColor(overlayBorderColor);
        g2.draw(getSplitVisualShape(bounds, position, false));
    }

    private static EditorStackDropTarget determineDropTarget(
        JComponent root,
        EditorStack sourceStack,
        int screenX,
        int screenY
    ) {
        var origin = new Point(screenX, screenY);
        SwingUtilities.convertPointFromScreen(origin, root);

        var stack = getStackAt(root, origin.x, origin.y);
        if (stack == null) {
            return null;
        }

        var point = new Point(screenX, screenY);
        SwingUtilities.convertPointFromScreen(point, stack);

        var bounds = stack.getComponentAt(0).getBounds();
        if (bounds.contains(point)) {
            // Hovering over the content area
            if (sourceStack == stack && stack.getTabCount() < 2) {
                return null;
            }
            for (EditorStack.Position position : EditorStack.Position.values()) {
                if (position == EditorStack.Position.CENTER && sourceStack == stack) {
                    // When moving within the same stack, ignore center splits
                    continue;
                }
                if (getSplitHoverShape(bounds, position).contains(point)) {
                    return new EditorStackDropTarget.Split(stack, position);
                }
            }
        } else {
            // Hovering over the tab area
            int index = getTabIndex(stack, point);
            if (index < 0) {
                return null;
            }
            if (sourceStack == stack) {
                // When moving within the same stack, ignore moves that don't change the tab order
                int selection = stack.getSelectedIndex();
                if (index == selection || index == selection + 1) {
                    return null;
                }
            }
            return new EditorStackDropTarget.Move(stack, index);
        }

        return null;
    }

    private static Shape getSplitHoverShape(Rectangle b, EditorStack.Position position) {
        int bw = b.x + b.width;
        int bh = b.y + b.height;
        int bw2 = b.x + b.width / 2;
        int bh2 = b.y + b.height / 2;

        return switch (position) {
            case TOP -> new Polygon(new int[]{b.x, bw2, bw}, new int[]{b.y, bh2, b.y}, 3);
            case BOTTOM -> new Polygon(new int[]{b.x, bw2, bw}, new int[]{bh, bh2, bh}, 3);
            case LEFT -> new Polygon(new int[]{b.x, bw2, b.x}, new int[]{b.y, bh2, bh}, 3);
            case RIGHT -> new Polygon(new int[]{bw, bw2, bw}, new int[]{b.y, bh2, bh}, 3);
            case CENTER -> new Polygon(
                new int[]{b.x + b.width / 4, bw - b.width / 4, bw - b.width / 4, b.x + b.width / 4},
                new int[]{b.y + b.height / 4, b.y + b.height / 4, bh - b.height / 4, bh - b.height / 4},
                4);
        };
    }

    private static Shape getSplitVisualShape(Rectangle rect, EditorStack.Position position, boolean fill) {
        int x = rect.x + TAB_OVERLAY_PADDING;
        int y = rect.y + TAB_OVERLAY_PADDING;
        int w = rect.width;
        int h = rect.height;
        int w2 = rect.width / 2;
        int h2 = rect.height / 2;
        int inset = TAB_OVERLAY_PADDING * 2 + (fill ? 0 : 1);

        return switch (position) {
            case TOP -> new RoundRectangle2D.Float(
                x,
                y,
                w - inset,
                h2 - inset,
                TAB_OVERLAY_ARC,
                TAB_OVERLAY_ARC);
            case BOTTOM -> new RoundRectangle2D.Float(
                x,
                y + h - h2,
                w - inset,
                h2 - inset,
                TAB_OVERLAY_ARC,
                TAB_OVERLAY_ARC);
            case LEFT -> new RoundRectangle2D.Float(
                x,
                y,
                w2 - inset,
                h - inset,
                TAB_OVERLAY_ARC,
                TAB_OVERLAY_ARC);
            case RIGHT -> new RoundRectangle2D.Float(
                x + w - w2,
                y,
                w2 - inset,
                h - inset,
                TAB_OVERLAY_ARC,
                TAB_OVERLAY_ARC);
            case CENTER -> new RoundRectangle2D.Float(
                x,
                y,
                w - inset,
                h - inset,
                TAB_OVERLAY_ARC,
                TAB_OVERLAY_ARC);
        };
    }

    private static Shape getTabVisualShape(Rectangle rect, boolean fill) {
        int inset = TAB_OVERLAY_PADDING * 2 + (fill ? 0 : 1);
        return new RoundRectangle2D.Float(
            rect.x + TAB_OVERLAY_PADDING,
            rect.y + TAB_OVERLAY_PADDING,
            rect.width - inset,
            rect.height - inset,
            TAB_OVERLAY_ARC,
            TAB_OVERLAY_ARC);
    }

    private static int getTabIndex(EditorStack target, Point point) {
        var isTopOrBottom = target.getTabPlacement() == JTabbedPane.TOP || target.getTabPlacement() == JTabbedPane.BOTTOM;
        int tabCount = target.getTabCount();

        for (int i = 0; i < tabCount; i++) {
            var bounds = target.getBoundsAt(i);
            if (isTopOrBottom) {
                bounds.setRect(bounds.x - bounds.width / 2.0, bounds.y, bounds.width, bounds.height);
            } else {
                bounds.setRect(bounds.x, bounds.y - bounds.height / 2.0, bounds.width, bounds.height);
            }
            if (bounds.contains(point)) {
                return i;
            }
        }

        var bounds = target.getBoundsAt(tabCount - 1);
        if (isTopOrBottom) {
            int tabX = bounds.x + bounds.width / 2;
            bounds.setRect(tabX, bounds.y, target.getWidth() - tabX, bounds.height);
        } else {
            int tabY = bounds.y + bounds.height / 2;
            bounds.setRect(bounds.x, tabY, bounds.width, target.getHeight() - tabY);
        }
        if (bounds.contains(point)) {
            return tabCount;
        }

        return -1;
    }

    private static EditorStack getStackAt(Component parent, int x, int y) {
        if (!parent.contains(x, y)) {
            return null;
        }

        if (parent instanceof JComponent jc) {
            for (Component child : jc.getComponents()) {
                if (child == null || !child.isVisible()) {
                    continue;
                }

                int cx = x - child.getX();
                int cy = y - child.getY();

                var result = getStackAt(child, cx, cy);
                if (result != null) {
                    return result;
                }
            }
        }

        if (parent instanceof EditorStack stack) {
            return stack;
        }

        return null;
    }
}
