package sh.adelessfox.odradek.app.ui.tree;

import sh.adelessfox.odradek.app.ui.util.Listeners;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public abstract class AbstractTreeModel implements TreeModel {
    private final Listeners<TreeModelListener> listeners = new Listeners<>(TreeModelListener.class);

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("valueForPathChanged");
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    public void nodeStructureChanged(TreePath path) {
        listeners.broadcast().treeStructureChanged(new TreeModelEvent(this, path, null, null));
    }

    public abstract void unload(Object parent);

    protected Listeners<TreeModelListener> listeners() {
        return listeners;
    }
}
