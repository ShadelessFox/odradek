package sh.adelessfox.odradek.app;

import sh.adelessfox.odradek.app.ui.tree.TreeStructure;

import java.util.List;
import java.util.function.Predicate;

public final class FilteredStructure<T> implements TreeStructure<T> {
    private final TreeStructure<T> delegate;
    private final Predicate<T> predicate;

    public FilteredStructure(TreeStructure<T> delegate, Predicate<T> predicate) {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    @Override
    public T getRoot() {
        return delegate.getRoot();
    }

    @Override
    public List<? extends T> getChildren(T parent) {
        return delegate.getChildren(parent).stream()
            .filter(predicate)
            .toList();
    }

    @Override
    public boolean hasChildren(T parent) {
        return delegate.hasChildren(parent);
    }
}
