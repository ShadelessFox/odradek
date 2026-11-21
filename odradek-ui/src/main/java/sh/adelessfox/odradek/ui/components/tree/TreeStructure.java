package sh.adelessfox.odradek.ui.components.tree;

import java.util.List;

public interface TreeStructure<T extends TreeStructure<T>> {
    List<? extends T> getChildren();

    boolean hasChildren();

    @Override
    boolean equals(Object other);

    /**
     * Returns whether this structure and {@code other} represent the same logical node.
     * <p>
     * Identity is determined by stable identifying properties, such as an ID or key,
     * rather than presentation or other state that may change between tree refreshes.
     * For example, renaming a node should not change its identity.
     * <p>
     * Use this comparison to match nodes across refreshes, such as when preserving
     * selection. Use {@link #equals(Object)} to determine whether a node's state
     * has changed.
     *
     * @param other the structure to compare with
     * @return {@code true} if both structures represent the same logical node; {@code false} otherwise
     * @implSpec The default implementation delegates to {@link #equals(Object)}.
     * Implementations whose equality includes non-identifying state should override
     * this method to compare only the properties that define the node's identity.
     */
    default boolean hasSameIdentity(T other) {
        return equals(other);
    }
}
