package sh.adelessfox.odradek.ui.tree;

import javax.swing.tree.TreePath;
import java.awt.event.InputEvent;
import java.util.EventObject;

public class TreeActionEvent extends EventObject {
    private final TreePath path;
    private final int row;

    public TreeActionEvent(InputEvent source, TreePath path, int row) {
        super(source);
        this.path = path;
        this.row = row;
    }

    @Override
    public InputEvent getSource() {
        return (InputEvent) super.getSource();
    }

    public TreePath getPath() {
        return path;
    }

    public int getRow() {
        return row;
    }

    public Object getLastPathComponent() {
        return path.getLastPathComponent();
    }
}
