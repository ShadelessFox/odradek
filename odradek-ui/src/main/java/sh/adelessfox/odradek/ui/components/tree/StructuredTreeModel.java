package sh.adelessfox.odradek.ui.components.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.ui.util.Listeners;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;
import java.util.function.Predicate;

/**
 * A concrete implementation of {@link javax.swing.tree.TreeModel} that uses
 * a {@link TreeStructure} to populate the tree.
 * <p>
 * It supports filtering of the elements in the tree using a {@link Predicate}
 * set via {@link #setFilter(Predicate)}. After setting the filter, the
 * tree can be updated by calling {@link #update()}.
 *
 * @param <T> the type of the elements in the tree
 * @see TreeStructure
 */
public final class StructuredTreeModel<T extends TreeStructure<T>> implements TreeModel {
    private static final Logger log = LoggerFactory.getLogger(StructuredTreeModel.class);

    private final Listeners<TreeModelListener> listeners = new Listeners<>(TreeModelListener.class);
    private final TreeStructure<T> root;
    private Predicate<T> filter;
    private Node<T> rootNode;

    public StructuredTreeModel(TreeStructure<T> root) {
        this.root = root;
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
            return !node.structure.hasChildren();
        }
        return node.children.isEmpty();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        Node<T> parentNode = cast(parent);
        Node<T> childNode = cast(child);
        return parentNode.equals(childNode.parent) ? parentNode.children.indexOf(childNode) : -1;
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

    public void update() {
        if (rootNode != null) {
            update(rootNode);
        }
    }

    public void update(TreePath path) {
        Node<T> node = cast(path.getLastPathComponent());
        update(node);
    }

    public Predicate<T> getFilter() {
        return filter;
    }

    public void setFilter(Predicate<T> filter) {
        this.filter = filter;
    }

    private void update(Node<T> node) {
        if (node.children == null) {
            return;
        }

        var newChildren = computeChildren(node);
        var oldChildren = node.children.stream().map(n -> n.structure).toList();

        var added = new ArrayList<Integer>();
        var removed = new ArrayList<Integer>();
        var unchanged = new HashMap<TreeStructure<T>, Integer>();

        difference(oldChildren, newChildren, added, removed, unchanged);

        if (!added.isEmpty() || !removed.isEmpty()) {
            var nodes = new ArrayList<Node<T>>();

            for (T child : newChildren) {
                var oldNodeIndex = unchanged.get(child);
                if (oldNodeIndex != null) {
                    nodes.add(node.children.get(oldNodeIndex));
                } else {
                    nodes.add(new Node<>(child, node, null));
                }
            }

            node.children = List.copyOf(nodes);

            if (added.isEmpty() && removed.size() == oldChildren.size() ||
                removed.isEmpty() && added.size() == newChildren.size()
            ) {
                treeStructureChanged(node);
                return;
            }

            if (!removed.isEmpty()) {
                treeNodesRemoved(node, removed.stream().mapToInt(i -> i).toArray());
            }

            if (!added.isEmpty()) {
                treeNodesInserted(node, added.stream().mapToInt(i -> i).toArray());
            }
        }

        for (Node<T> child : node.children) {
            update(child);
        }
    }

    private static <T> void difference(
        List<? extends T> original,
        List<? extends T> updated,
        List<Integer> added,
        List<Integer> removed,
        Map<? super T, Integer> unchanged
    ) {
        Set<T> matched = new HashSet<>();

        int length = Math.min(original.size(), updated.size());
        for (int i = 0; i < length; i++) {
            T originalItem = original.get(i);
            T updatedItem = updated.get(i);

            if (originalItem.equals(updatedItem)) {
                unchanged.put(originalItem, i);
                matched.add(originalItem);
            }
        }

        for (int i = 0; i < original.size(); i++) {
            T item = original.get(i);
            if (!matched.contains(item)) {
                removed.add(i);
            }
        }

        for (int i = 0; i < updated.size(); i++) {
            T item = updated.get(i);
            if (!matched.contains(item)) {
                added.add(i);
            }
        }
    }

    private void treeNodesInserted(Node<T> parent, int[] children) {
        var path = getNodePath(parent);
        var event = new TreeModelEvent(this, path, children, null);
        listeners.broadcast().treeNodesInserted(event);
    }

    private void treeNodesRemoved(Node<T> parent, int[] children) {
        var path = getNodePath(parent);
        var event = new TreeModelEvent(this, path, children, null);
        listeners.broadcast().treeNodesRemoved(event);
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
        if (rootNode == null) {
            rootNode = computeRootNode();
        }
        return rootNode;
    }

    private List<Node<T>> getChildNodes(Node<T> parent) {
        if (parent.children == null) {
            if (isLeaf(parent)) {
                parent.children = List.of();
            } else {
                parent.children = computeChildNodes(parent);
            }
        }
        return parent.children;
    }

    private Node<T> computeRootNode() {
        log.debug("Computing root for {}", root);
        return new Node<>(root, null);
    }

    private List<Node<T>> computeChildNodes(Node<T> parent) {
        return computeChildren(parent).stream()
            .map(child -> new Node<>(child, parent))
            .toList();
    }

    private List<? extends T> computeChildren(Node<T> parent) {
        log.debug("Computing children of {} for {}", parent.structure, root);
        var children = parent.structure.getChildren();
        if (filter != null) {
            return children.stream().filter(filter).toList();
        } else {
            return children;
        }
    }

    private static class Node<T extends TreeStructure<T>> implements TreeItem<TreeStructure<T>> {
        private final TreeStructure<T> structure;
        private final Node<T> parent;
        private List<Node<T>> children;

        Node(TreeStructure<T> structure, Node<T> parent) {
            this(structure, parent, null);
        }

        Node(TreeStructure<T> structure, Node<T> parent, List<Node<T>> children) {
            this.structure = structure;
            this.parent = parent;
            this.children = children;
        }

        @Override
        public TreeStructure<T> getValue() {
            return structure;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Node<?> node
                && Objects.equals(structure, node.structure)
                && Objects.equals(parent, node.parent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(structure, parent);
        }

        @Override
        public String toString() {
            return structure.toString();
        }
    }
}
