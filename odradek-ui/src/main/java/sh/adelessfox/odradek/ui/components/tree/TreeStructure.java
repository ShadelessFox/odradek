package sh.adelessfox.odradek.ui.components.tree;

import java.util.List;

public interface TreeStructure<T extends TreeStructure<T>> {
    List<? extends T> getChildren();

    boolean hasChildren();
}
