package sh.adelessfox.odradek.app.ui.tools.bookmarks.menu;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.app.ui.bookmarks.FolderId;
import sh.adelessfox.odradek.app.ui.tools.bookmarks.BookmarkStructure;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.util.Gatherers;

import javax.swing.*;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class AbstractBookmarkAction extends Action {
    protected static String promptName(String title, String message, String name) {
        while (true) {
            name = (String) JOptionPane.showInputDialog(
                JOptionPane.getRootFrame(),
                message,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                name
            );
            if (name == null) {
                return null;
            }
            if (name.isBlank()) {
                JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "Name cannot be empty",
                    title,
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }
            return name.strip();
        }
    }

    protected static Optional<FolderId> enclosingFolder(ActionContext context) {
        var folders = context.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(BookmarkStructure.class))
            .flatMap(structure -> folderIdOf(structure).stream())
            .limit(2).collect(Collectors.toSet());
        return switch (folders.size()) {
            case 1 -> Optional.of(folders.iterator().next());
            default -> Optional.empty();
        };
    }

    private static Optional<FolderId> folderIdOf(BookmarkStructure structure) {
        return switch (structure) {
            case BookmarkStructure.Bookmark bookmark -> Optional.of(bookmarks().getParent(bookmark.id()));
            case BookmarkStructure.Folder folder -> Optional.of(folder.id());
        };
    }

    protected static Stream<ObjectId> selectedObjects(ActionContext context) {
        return context.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(ObjectIdHolder.class))
            .map(ObjectIdHolder::objectId);
    }

    protected static Bookmarks bookmarks() {
        return Application.getInstance().bookmarks();
    }
}
