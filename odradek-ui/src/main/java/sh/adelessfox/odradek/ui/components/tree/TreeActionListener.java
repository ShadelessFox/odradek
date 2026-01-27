package sh.adelessfox.odradek.ui.components.tree;

import java.util.EventListener;
import java.util.function.Consumer;

public interface TreeActionListener extends EventListener {
    static TreeActionListener treePathClickedAdapter(Consumer<TreeActionEvent> consumer) {
        return new TreeActionListener() {
            @Override
            public void treePathClicked(TreeActionEvent event) {
                consumer.accept(event);
            }
        };
    }

    static TreeActionListener treePathSelectedAdapter(Consumer<TreeActionEvent> consumer) {
        return new TreeActionListener() {
            @Override
            public void treePathSelected(TreeActionEvent event) {
                consumer.accept(event);
            }
        };
    }

    default void treePathClicked(TreeActionEvent event) {
        // do nothing by default
    }

    default void treePathSelected(TreeActionEvent event) {
        // do nothing by default
    }
}
