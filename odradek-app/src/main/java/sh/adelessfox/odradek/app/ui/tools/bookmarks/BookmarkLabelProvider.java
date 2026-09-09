package sh.adelessfox.odradek.app.ui.tools.bookmarks;

import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.util.Optional;

final class BookmarkLabelProvider implements StyledTreeLabelProvider<BookmarkStructure> {
    private final Bookmarks bookmarks;

    BookmarkLabelProvider(Bookmarks bookmarks) {
        this.bookmarks = bookmarks;
    }

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
            case BookmarkStructure.GroupBookmark(_, var key, var name) -> StyledText.builder()
                .add(String.valueOf(key.groupId()), StyledFragment.GRAYED).add(" ").add(name)
                .build();
            case BookmarkStructure.ObjectBookmark(_, var key, var name) -> StyledText.builder()
                .add(key.objectId().toString(), StyledFragment.GRAYED).add(" ").add(name)
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
            case BookmarkStructure.GroupBookmark _ -> Optional.of(Fugue.getIcon("folder-bookmark"));
            case BookmarkStructure.ObjectBookmark _ -> Optional.of(Fugue.getIcon("document-bookmark"));
        };
    }

    private boolean isRootFolder(BookmarkStructure.Folder folder) {
        return folder.id().equals(bookmarks.rootFolderId());
    }
}
