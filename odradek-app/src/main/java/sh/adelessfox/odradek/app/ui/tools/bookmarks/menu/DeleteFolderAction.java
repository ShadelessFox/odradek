package sh.adelessfox.odradek.app.ui.tools.bookmarks.menu;

import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.app.ui.tools.bookmarks.BookmarkStructure;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

import javax.swing.*;

@ActionRegistration(text = "Delete Folder", icon = "fugue:folder--minus", keystroke = "DELETE")
@ActionContribution(parent = BookmarkMenu.ID, group = MenuIds.GROUP_UTIL, order = 1)
public final class DeleteFolderAction extends AbstractBookmarkAction {
    @Override
    public void perform(ActionContext context) {
        var folder = context.get(DataKeys.SELECTION, BookmarkStructure.Folder.class).orElseThrow();
        if (!needsConfirmation(folder) || confirmDeletion(folder)) {
            bookmarks().deleteFolder(folder.id());
        }
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, BookmarkStructure.Folder.class)
            .filter(folder -> !folder.id().equals(bookmarks().rootFolderId()))
            .isPresent();
    }

    private static boolean needsConfirmation(BookmarkStructure.Folder folder) {
        var bookmarks = bookmarks();
        return !bookmarks.getAllInFolder(folder.id()).isEmpty()
            || !bookmarks.getAllFoldersInFolder(folder.id()).isEmpty();
    }

    private static boolean confirmDeletion(BookmarkStructure.Folder folder) {
        return JOptionPane.showConfirmDialog(
            JOptionPane.getRootFrame(),
            "Are you sure you want to delete folder '" + folder.name() + "' and all its contents?",
            "Delete Folder",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }
}
