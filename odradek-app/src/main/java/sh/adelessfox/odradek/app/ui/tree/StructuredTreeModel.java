package sh.adelessfox.odradek.app.ui.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.util.Listeners;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A concrete implementation of {@link javax.swing.tree.TreeModel} that uses
 * a {@link TreeStructure} to populate the tree.
 *
 * @param <T> the type of the elements in the tree
 * @see TreeStructure
 */
public final class StructuredTreeModel<T> implements TreeModel {
    private static final Logger log = LoggerFactory.getLogger(StructuredTreeModel.class);

    private final Listeners<TreeModelListener> listeners = new Listeners<>(TreeModelListener.class);
    private final TreeStructure<T> structure;
    private Node<T> root;

    public StructuredTreeModel(TreeStructure<T> structure) {
        this.structure = structure;
    }

    @Override
    public Object getRoot() {
        return getRootNode();
    }

    @Override
    public Object getChild(Object parent, int index) {
        Node<T> node = cast(parent);
        return getChildNodes(node).get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        Node<T> node = cast(parent);
        return getChildNodes(node).size();
    }

    @Override
    public boolean isLeaf(Object parent) {
        Node<T> node = cast(parent);
        if (node.children == null) {
            return !structure.hasChildren(node.element);
        }
        return node.children.isEmpty();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        Node<T> node = cast(child);
        return node.index;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("valueForPathChanged");
    }

    public void reload() {
        if (root != null) {
            reload(root);
        }
    }

    public void reload(TreePath path) {
        Node<T> node = cast(path.getLastPathComponent());
        reload(node);
    }

    private void reload(Node<T> node) {
        // TODO: Reinvalidate the cache against the structure; don't just
        //       hard-reload from the root. We're losing expanded state
        //       and it's also pretty expensive
        if (node.children != null) {
            node.children = null;
            treeStructureChanged(node);
        }
    }

    private void treeStructureChanged(Node<T> node) {
        var path = getNodePath(node);
        var event = new TreeModelEvent(this, path, null, null);
        listeners.broadcast().treeStructureChanged(event);
    }

    @SuppressWarnings("unchecked")
    private Node<T> cast(Object node) {
        return (Node<T>) node;
    }

    private TreePath getNodePath(Node<T> node) {
        if (node.parent != null) {
            return getNodePath(node.parent).pathByAddingChild(node);
        } else {
            return new TreePath(node);
        }
    }

    private Object getRootNode() {
        if (root == null) {
            root = computeRootNode();
        }
        return root;
    }

    private List<Node<T>> getChildNodes(Node<T> parent) {
        if (parent.children == null) {
            if (isLeaf(parent)) {
                parent.children = List.of();
            } else {
                parent.children = computeChildrenNodes(parent);
            }
        }
        return parent.children;
    }

    private Node<T> computeRootNode() {
        log.debug("Computing root for {}", structure);
        return new Node<>(structure.getRoot(), null, 0);
    }

    private List<Node<T>> computeChildrenNodes(Node<T> parent) {
        log.debug("Computing children of {} for {}", parent.element, structure);
        var children = structure.getChildren(parent.element);
        var nodes = new ArrayList<Node<T>>(children.size());
        for (int i = 0; i < children.size(); i++) {
            nodes.add(new Node<>(children.get(i), parent, i));
        }
        return List.copyOf(nodes);
    }

    private static class Node<T> implements TreeItem<T> {
        private final T element;
        private final Node<T> parent;
        private List<Node<T>> children;
        private final int index;

        Node(T element, Node<T> parent, int index) {
            this.element = element;
            this.parent = parent;
            this.index = index;
        }

        @Override
        public T getValue() {
            return element;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Node<?> node && Objects.equals(element, node.element);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(element);
        }

        @Override
        public String toString() {
            return element.toString();
        }
    }
}
