package sh.adelessfox.odradek.app.ui.tools.bookmarks;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.util.Optional;

final class BookmarkLabelProvider implements StyledTreeLabelProvider<BookmarkStructure> {
    @Override
    public Optional<StyledText> getStyledText(BookmarkStructure element) {
        return switch (element) {
            case BookmarkStructure.Folder f -> {
                if (isRootFolder(f)) {
                    yield StyledText.builder()
                        .add("User bookmarks", StyledFragment.GRAYED)
                        .build();
                } else {
                    yield StyledText.builder()
                        .add(f.name())
                        .build();
                }
            }
            case BookmarkStructure.Bookmark b -> StyledText.builder()
                .add(b.id().toString(), StyledFragment.GRAYED).add(" ").add(b.name())
                .build();
        };
    }

    @Override
    public Optional<Icon> getIcon(BookmarkStructure element) {
        return switch (element) {
            case BookmarkStructure.Folder f -> {
                if (isRootFolder(f)) {
                    yield Optional.empty();
                } else {
                    yield Optional.of(Fugue.getIcon("folder"));
                }
            }
            case BookmarkStructure.Bookmark _ -> Optional.of(Fugue.getIcon("blue-document"));
        };
    }

    private boolean isRootFolder(BookmarkStructure.Folder folder) {
        var bookmarks = Application.getInstance().bookmarks();
        return folder.id().equals(bookmarks.rootFolderId());
    }
}
