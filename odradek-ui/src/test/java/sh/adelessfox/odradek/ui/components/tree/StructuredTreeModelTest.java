package sh.adelessfox.odradek.ui.components.tree;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class StructuredTreeModelTest {
    @Test
    void movingSelectedChildPreservesSelectionWithoutDuplicatingIt() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var child = new Structure("child", false);
            var remaining = new Structure("remaining", false);
            var source = new Structure("source", true, remaining, child);
            var target = new Structure("target", true);
            var root = new Structure("root", true, source, target);
            var tree = new StructuredTree<>(root);
            var model = tree.getModel();
            var removedChildren = new AtomicReference<Object[]>();
            model.addTreeModelListener(new TreeModelListener() {
                @Override
                public void treeNodesChanged(TreeModelEvent e) {
                }

                @Override
                public void treeNodesInserted(TreeModelEvent e) {
                }

                @Override
                public void treeNodesRemoved(TreeModelEvent e) {
                    removedChildren.set(e.getChildren());
                }

                @Override
                public void treeStructureChanged(TreeModelEvent e) {
                }
            });

            var rootItem = model.getRoot();
            var sourceItem = model.getChild(rootItem, 0);
            var targetItem = model.getChild(rootItem, 1);
            var childItem = model.getChild(sourceItem, 1);
            var sourcePath = new TreePath(new Object[]{rootItem, sourceItem});
            var targetPath = new TreePath(new Object[]{rootItem, targetItem});

            assertEquals(sourcePath, model.findLoadedPath(element -> element == source).orElseThrow());
            assertEquals(targetPath, model.findLoadedPath(element -> element == target).orElseThrow());

            tree.expandPath(sourcePath);
            tree.expandPath(targetPath);
            tree.setSelectionPath(sourcePath.pathByAddingChild(childItem));

            source.children.remove(child);
            target.children.add(child);
            tree.updatePreservingSelection((first, second) -> first.id.equals(second.id), model::refresh);

            assertEquals(1, model.getChildCount(sourceItem));
            assertEquals(1, model.getChildCount(targetItem));
            assertEquals(1, visibleOccurrences(tree, child));
            assertSame(child, tree.getSelectionPathComponent());
            assertEquals(targetPath, tree.getSelectionPath().getParentPath());
            assertEquals(1, removedChildren.get().length);
            assertSame(childItem, removedChildren.get()[0]);

            tree.collapsePath(sourcePath);
            tree.expandPath(sourcePath);
            assertEquals(1, visibleOccurrences(tree, child));
        });
    }

    @Test
    void renamingSelectedChildPreservesSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var child = new Structure("child", "Old name", false);
            var root = new Structure("root", true, child);
            var tree = new StructuredTree<>(root);
            var model = tree.getModel();
            var rootItem = model.getRoot();
            var childItem = model.getChild(rootItem, 0);
            tree.setSelectionPath(new TreePath(new Object[]{rootItem, childItem}));

            var renamed = new Structure("child", "New name", false);
            root.children.set(0, renamed);
            tree.updatePreservingSelection((first, second) -> first.id.equals(second.id), model::refresh);

            assertSame(renamed, tree.getSelectionPathComponent());
        });
    }

    private static int visibleOccurrences(JTree tree, Structure structure) {
        int occurrences = 0;
        for (int row = 0; row < tree.getRowCount(); row++) {
            var component = tree.getPathForRow(row).getLastPathComponent();
            if (component instanceof TreeItem<?> item && item.getValue() == structure) {
                occurrences++;
            }
        }
        return occurrences;
    }

    private static final class Structure implements TreeStructure<Structure> {
        private final String id;
        private final String name;
        private final boolean folder;
        private final List<Structure> children = new ArrayList<>();

        private Structure(String name, boolean folder, Structure... children) {
            this(name, name, folder, children);
        }

        private Structure(String id, String name, boolean folder, Structure... children) {
            this.id = id;
            this.name = name;
            this.folder = folder;
            this.children.addAll(List.of(children));
        }

        @Override
        public List<? extends Structure> getChildren() {
            return List.copyOf(children);
        }

        @Override
        public boolean hasChildren() {
            return folder;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
