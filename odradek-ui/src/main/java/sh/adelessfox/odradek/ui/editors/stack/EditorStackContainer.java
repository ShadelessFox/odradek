package sh.adelessfox.odradek.ui.editors.stack;

import javax.swing.*;
import java.awt.*;

/**
 * Hosts a single {@link EditorStack} or a split pane with two {@link EditorStackContainer}s.
 */
public class EditorStackContainer extends JComponent {
    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    public record SplitResult(EditorStackContainer left, EditorStackContainer right) {
        public EditorStack targetStack() {
            return (EditorStack) right.getComponent(0);
        }
    }

    private final EditorStackManager manager;

    EditorStackContainer(EditorStackManager manager, EditorStack stack) {
        this.manager = manager;
        setLayout(new BorderLayout());
        add(stack, BorderLayout.CENTER);
    }

    public SplitResult split(Orientation orientation, double position, boolean leading) {
        var first = new EditorStackContainer(manager, getEditorStack());
        var second = new EditorStackContainer(manager, manager.createStack());

        var pane = new JSplitPane(orientation == Orientation.HORIZONTAL ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT);
        pane.setLeftComponent(leading ? second : first);
        pane.setRightComponent(leading ? first : second);

        removeAll();
        add(pane, BorderLayout.CENTER);
        validate();

        pane.setResizeWeight(position);
        pane.setDividerLocation(position);

        return new SplitResult(first, second);
    }

    public void compact() {
        Component leaf = getComponent(0);

        if (leaf instanceof JSplitPane pane) {
            Component left = pane.getLeftComponent();
            Component right = pane.getRightComponent();

            if (canCompact(left)) {
                leaf = right;
            } else if (canCompact(right)) {
                leaf = left;
            }
        }

        while (leaf instanceof EditorStackContainer container) {
            leaf = container.getComponent(0);
        }

        if (leaf != null && getComponent(0) != leaf) {
            removeAll();
            add(leaf, BorderLayout.CENTER);

            revalidate();
            invalidate();
        }

        EditorStackContainer parent = (EditorStackContainer) SwingUtilities.getAncestorOfClass(EditorStackContainer.class, this);
        if (parent != null) {
            parent.compact();
        }
    }

    public boolean isSplit() {
        return getComponent(0) instanceof JSplitPane;
    }

    public boolean isLeaf() {
        return getComponent(0) instanceof EditorStack;
    }

    public EditorStack getEditorStack() {
        assert isLeaf();
        return (EditorStack) getComponent(0);
    }

    public EditorStackContainer getLeftContainer() {
        assert isSplit();
        return (EditorStackContainer) ((JSplitPane) getComponent(0)).getLeftComponent();
    }

    public EditorStackContainer getRightContainer() {
        assert isSplit();
        return (EditorStackContainer) ((JSplitPane) getComponent(0)).getRightComponent();
    }

    public void layoutContainer() {
        layoutContainer(this);
    }

    private static void layoutContainer(EditorStackContainer container) {
        if (container.getComponent(0) instanceof JSplitPane pane) {
            pane.setDividerLocation(pane.getResizeWeight());
            pane.validate();

            layoutContainer((EditorStackContainer) pane.getLeftComponent());
            layoutContainer((EditorStackContainer) pane.getRightComponent());
        }
    }

    private static boolean canCompact(Component component) {
        if (component instanceof JSplitPane pane) {
            return canCompact(pane.getLeftComponent()) && canCompact(pane.getRightComponent());
        } else if (component instanceof EditorStackContainer pane) {
            return canCompact(pane.getComponent(0));
        } else {
            return ((EditorStack) component).getTabCount() == 0;
        }
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (getComponentCount() > 0) {
            throw new IllegalStateException("Container already contains a component");
        }
        super.addImpl(comp, constraints, index);
    }
}
