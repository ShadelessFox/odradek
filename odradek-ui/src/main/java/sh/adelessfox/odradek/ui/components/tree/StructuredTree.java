package sh.adelessfox.odradek.ui.components.tree;

import com.formdev.flatlaf.util.UIScale;
import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.util.Listeners;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class StructuredTree<T> extends JTree implements DataContext {
    private final Listeners<TreeActionListener> actionListeners = new Listeners<>(TreeActionListener.class);
    private TreeLabelProvider<T> labelProvider;

    // For caching last shown tooltip while hovering over the same row
    private int lastRowIndex = -1;
    private int lastRowCount = -1;

    public StructuredTree(TreeStructure<T> structure) {
        super(new StructuredTreeModel<>(structure));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() % getToggleClickCount() == 0) {
                    fireActionListener(e);
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    fireActionListener(e);
                }
            }
        });

        setLargeModel(true);

        // Required for TreeLabelProvider#getToolTip
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public Optional<Object> get(String key) {
        if (DataKeys.COMPONENT.is(key)) {
            return Optional.of(this);
        }
        if (DataKeys.SELECTION.is(key)) {
            return Optional.ofNullable(getSelectionComponent(getSelectionPath()));
        }
        if (DataKeys.SELECTION_LIST.is(key)) {
            TreePath[] paths = getSelectionPaths();
            if (paths != null) {
                return Optional.of(Arrays.stream(paths).map(this::getSelectionComponent).toList());
            }
        }
        return Optional.empty();
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        if (event == null) {
            return null;
        }
        int offset = UIScale.scale(10);
        return new Point(event.getX() + offset, event.getY() + offset);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getToolTipText(MouseEvent event) {
        if (event == null) {
            return null;
        }

        if (labelProvider != null) {
            int rowIndex = getRowForLocation(event.getX(), event.getY());
            if (rowIndex < 0) {
                return null;
            }

            int visibleRows = getRowCount();
            if (lastRowIndex == rowIndex && lastRowCount == visibleRows) {
                return super.getToolTipText();
            }

            var path = getPathForRow(rowIndex);
            if (path == null) {
                return null;
            }

            var element = (T) getElement(path.getLastPathComponent());
            if (element == null) {
                return null;
            }

            lastRowIndex = rowIndex;
            lastRowCount = visibleRows;
            putClientProperty(TOOL_TIP_TEXT_KEY, labelProvider.getToolTip(element).orElse(null));

            return super.getToolTipText();
        }

        return super.getToolTipText(event);
    }

    @Override
    @SuppressWarnings("unchecked")
    public StructuredTreeModel<T> getModel() {
        return (StructuredTreeModel<T>) super.getModel();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setModel(TreeModel newModel) {
        setModel((StructuredTreeModel<T>) newModel);
    }

    public void setModel(StructuredTreeModel<T> newModel) {
        super.setModel(newModel);
    }

    public TreeLabelProvider<T> getLabelProvider() {
        return labelProvider;
    }

    public void setLabelProvider(TreeLabelProvider<T> labelProvider) {
        if (this.labelProvider != labelProvider) {
            this.labelProvider = labelProvider;

            if (labelProvider != null) {
                setCellRenderer(new LabelProviderTreeCellRenderer<>(labelProvider));
            } else {
                setCellRenderer(null);
            }
        }
    }

    public void addActionListener(TreeActionListener listener) {
        Objects.requireNonNull(listener);
        actionListeners.add(listener);
    }

    public void removeActionListener(TreeActionListener listener) {
        Objects.requireNonNull(listener);
        actionListeners.remove(listener);
    }

    public Object getSelectionPathComponent() {
        return getSelectionComponent(getSelectionPath());
    }

    private void fireActionListener(InputEvent event) {
        TreePath[] paths = getSelectionPaths();
        if (paths == null) {
            return;
        }
        for (TreePath path : paths) {
            fireActionListener(event, path, getRowForPath(path));
        }
    }

    private void fireActionListener(InputEvent event, TreePath path, int row) {
        actionListeners.broadcast().treePathSelected(new TreeActionEvent(event, path, row));
    }

    private Object getSelectionComponent(TreePath path) {
        if (path == null) {
            return null;
        }
        return getElement(path.getLastPathComponent());
    }

    private static Object getElement(Object value) {
        if (value instanceof TreeItem<?> item) {
            value = item.getValue();
        }
        return value;
    }

    private static class LabelProviderTreeCellRenderer<T> extends DefaultTreeCellRenderer {
        private final TreeLabelProvider<T> labelProvider;

        LabelProviderTreeCellRenderer(TreeLabelProvider<T> labelProvider) {
            this.labelProvider = labelProvider;
        }

        @Override
        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus
        ) {
            super.getTreeCellRendererComponent(tree, null, sel, expanded, leaf, row, hasFocus);

            @SuppressWarnings("unchecked")
            var element = (T) getElement(value);

            var text = labelProvider.getText(element).orElse(null);
            setText(text);

            var icon = labelProvider.getIcon(element).orElse(null);
            if (icon == null || tree.isEnabled()) {
                setIcon(icon);
            } else {
                setDisabledIcon(Objects.requireNonNullElse(UIManager.getLookAndFeel().getDisabledIcon(tree, icon), icon));
            }

            return this;
        }
    }
}
