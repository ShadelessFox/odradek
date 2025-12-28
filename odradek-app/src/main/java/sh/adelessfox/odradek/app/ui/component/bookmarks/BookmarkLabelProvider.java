package sh.adelessfox.odradek.app.ui.component.bookmarks;

import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;

import javax.swing.*;
import java.util.Optional;

final class BookmarkLabelProvider implements StyledTreeLabelProvider<BookmarkStructure> {
    @Override
    public Optional<StyledText> getStyledText(BookmarkStructure element) {
        return switch (element) {
            case BookmarkStructure.Root _ -> Optional.of(StyledText.builder()
                .add("User bookmarks", StyledFragment.GRAYED)
                .build());
            case BookmarkStructure.Bookmark b -> Optional.of(StyledText.builder()
                .add(b.id.toString(), StyledFragment.GRAYED).add(" ").add(b.name)
                .build());
        };
    }

    @Override
    public Optional<Icon> getIcon(BookmarkStructure element) {
        return Optional.empty();
    }
}
