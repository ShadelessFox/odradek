package sh.adelessfox.odradek.ui.components.tree;

import javax.swing.*;
import java.util.Optional;

public interface TreeLabelProvider<T> {
    Optional<String> getText(T element);

    default Optional<Icon> getIcon(T element) {
        return Optional.empty();
    }

    default Optional<String> getToolTip(T element) {
        return Optional.empty();
    }
}
