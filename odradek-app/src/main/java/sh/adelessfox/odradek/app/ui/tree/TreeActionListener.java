package sh.adelessfox.odradek.app.ui.tree;

import java.util.EventListener;

public interface TreeActionListener extends EventListener {
    void treePathSelected(TreeActionEvent event);
}
