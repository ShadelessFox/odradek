package sh.adelessfox.odradek.ui.editors.stack;

import sh.adelessfox.odradek.ui.components.Splitter;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * Hosts a single {@link EditorStack} or a split pane with two {@link EditorStackContainer}s.
 */
public final class EditorStackContainer extends JComponent {
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

    public SplitResult split(Splitter.Orientation orientation, double position, boolean leading) {
        var first = new EditorStackContainer(manager, getEditorStack());
        var second = new EditorStackContainer(manager, manager.createStack());

        var pane = new Splitter(orientation);
        pane.setFirstComponent(leading ? second : first);
        pane.setSecondComponent(leading ? first : second);

        removeAll();
        add(pane, BorderLayout.CENTER);
        validate();

        pane.setResizeWeight(position);
        pane.setDividerLocation(position);

        return new SplitResult(first, second);
    }

    public void compact() {
        Component leaf = getComponent(0);

        if (leaf instanceof Splitter pane) {
            Component left = pane.getFirstComponent();
            Component right = pane.getSecondComponent();

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

        EditorStackContainer parent = getParentContainer();
        if (parent != null) {
            parent.compact();
        }
    }

    public boolean isSplit() {
        return getComponent(0) instanceof Splitter;
    }

    public boolean isLeaf() {
        return getComponent(0) instanceof EditorStack;
    }

    public Splitter.Orientation getOrientation() {
        if (!(getComponent(0) instanceof Splitter pane)) {
            throw new IllegalStateException("Container is not split");
        }
        return pane.getOrientation();
    }

    public void setOrientation(Splitter.Orientation orientation) {
        if (!(getComponent(0) instanceof Splitter pane)) {
            throw new IllegalStateException("Container is not split");
        }
        double proportion = getProportion();
        pane.setOrientation(orientation);
        pane.setDividerLocation(proportion);
    }

    public double getProportion() {
        if (!(getComponent(0) instanceof Splitter pane)) {
            throw new IllegalStateException("Container is not split");
        }
        return pane.getDividerLocation();
    }

    public EditorStack getEditorStack() {
        if (!(getComponent(0) instanceof EditorStack stack)) {
            throw new IllegalStateException("Container is not a leaf");
        }
        return stack;
    }

    public EditorStackContainer getLeftContainer() {
        if (!(getComponent(0) instanceof Splitter pane)) {
            throw new IllegalStateException("Container is not split");
        }
        return (EditorStackContainer) pane.getFirstComponent();
    }

    public EditorStackContainer getRightContainer() {
        if (!(getComponent(0) instanceof Splitter pane)) {
            throw new IllegalStateException("Container is not split");
        }
        return (EditorStackContainer) pane.getSecondComponent();
    }

    public EditorStackContainer getSplitContainer() {
        EditorStackContainer container = this;
        while (container != null && !container.isSplit()) {
            container = container.getParentContainer();
        }
        return container;
    }

    public Optional<EditorStackContainer> getOpposite(EditorStackContainer container) {
        EditorStackContainer parent = getSplitContainer();
        if (parent == null) {
            return Optional.empty();
        }

        EditorStackContainer left = parent.getLeftContainer();
        EditorStackContainer right = parent.getRightContainer();

        if (left == container) {
            return Optional.of(right);
        } else if (right == container) {
            return Optional.of(left);
        } else {
            throw new IllegalStateException("Container is not a child of this container");
        }
    }

    private EditorStackContainer getParentContainer() {
        return (EditorStackContainer) SwingUtilities.getAncestorOfClass(EditorStackContainer.class, this);
    }

    public void layoutContainer() {
        layoutContainer(this);
    }

    private static void layoutContainer(EditorStackContainer container) {
        if (container.getComponent(0) instanceof Splitter pane) {
            pane.setDividerLocation(pane.getResizeWeight());
            pane.validate();

            layoutContainer((EditorStackContainer) pane.getFirstComponent());
            layoutContainer((EditorStackContainer) pane.getSecondComponent());
        }
    }

    private static boolean canCompact(Component component) {
        if (component instanceof Splitter pane) {
            return canCompact(pane.getFirstComponent()) && canCompact(pane.getSecondComponent());
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
