package sh.adelessfox.odradek.ui.tree;

import java.util.List;

public interface TreeStructure<T> {
    T getRoot();

    List<? extends T> getChildren(T parent);

    boolean hasChildren(T parent);
}
