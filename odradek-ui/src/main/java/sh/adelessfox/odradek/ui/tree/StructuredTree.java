package sh.adelessfox.odradek.ui.tree;

import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.util.Listeners;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.util.Objects;
import java.util.Optional;

public class StructuredTree<T> extends JTree implements DataContext {
    private final Listeners<TreeActionListener> actionListeners = new Listeners<>(TreeActionListener.class);

    public StructuredTree(StructuredTreeModel<T> model) {
        super(model);

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
    }

    @Override
    public Optional<Object> get(String key) {
        if (DataKeys.COMPONENT.is(key)) {
            return Optional.of(this);
        }
        if (DataKeys.SELECTION.is(key)) {
            Object component = getLastSelectedPathComponent();
            if (component instanceof TreeItem<?> item) {
                component = item.getValue();
            }
            return Optional.ofNullable(component);
        }
        return Optional.empty();
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

    public void addActionListener(TreeActionListener listener) {
        Objects.requireNonNull(listener);
        actionListeners.add(listener);
    }

    public void removeActionListener(TreeActionListener listener) {
        Objects.requireNonNull(listener);
        actionListeners.remove(listener);
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
}
