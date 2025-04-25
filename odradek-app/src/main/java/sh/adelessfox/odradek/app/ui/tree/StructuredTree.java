package sh.adelessfox.odradek.app.ui.tree;

import sh.adelessfox.odradek.app.ui.util.Listeners;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.util.Objects;

public class StructuredTree extends JTree {
    private final Listeners<TreeActionListener> actionListeners = new Listeners<>(TreeActionListener.class);

    public StructuredTree(StructuredTreeModel<?> model) {
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
    public StructuredTreeModel<?> getModel() {
        return (StructuredTreeModel<?>) super.getModel();
    }

    @Override
    public void setModel(TreeModel newModel) {
        setModel((StructuredTreeModel<?>) newModel);
    }

    public void setModel(StructuredTreeModel<?> newModel) {
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
        var path = getLeadSelectionPath();
        if (path != null) {
            fireActionListener(event, path, getLeadSelectionRow());
        }
    }

    private void fireActionListener(InputEvent event, TreePath path, int row) {
        actionListeners.broadcast().treePathSelected(new TreeActionEvent(event, path, row));
    }
}
