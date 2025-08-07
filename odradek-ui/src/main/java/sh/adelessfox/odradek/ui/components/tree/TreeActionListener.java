package sh.adelessfox.odradek.ui.components.tree;

import java.util.EventListener;

public interface TreeActionListener extends EventListener {
    void treePathSelected(TreeActionEvent event);
}
