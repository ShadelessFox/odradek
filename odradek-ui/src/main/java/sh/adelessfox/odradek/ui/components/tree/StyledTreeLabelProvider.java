package sh.adelessfox.odradek.ui.components.tree;

import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public interface StyledTreeLabelProvider<T> extends TreeLabelProvider<T> {
    Optional<StyledText> getStyledText(T element);

    @Override
    default Optional<String> getText(T element) {
        return getStyledText(element).map(StyledText::toString);
    }
}
