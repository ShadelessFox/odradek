package sh.adelessfox.odradek.ui.components.tree;

import javax.swing.tree.TreePath;
import java.util.EventObject;

public class TreeActionEvent extends EventObject {
    private final TreePath path;
    private final int row;

    public TreeActionEvent(EventObject source, TreePath path, int row) {
        super(source);
        this.path = path;
        this.row = row;
    }

    public TreePath getPath() {
        return path;
    }

    public int getRow() {
        return row;
    }

    public Object getLastPathComponent() {
        Object component = path.getLastPathComponent();
        if (component instanceof TreeItem<?> item) {
            return item.getValue();
        }
        return component;
    }
}
