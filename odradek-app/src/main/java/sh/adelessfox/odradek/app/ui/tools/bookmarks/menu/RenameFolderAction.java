package sh.adelessfox.odradek.app.ui.tools.bookmarks.menu;

import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.app.ui.tools.bookmarks.BookmarkStructure;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Rename Folder\u2026", icon = "fugue:folder--pencil", keystroke = "F2")
@ActionContribution(parent = BookmarkMenu.ID, group = MenuIds.GROUP_UTIL, order = 2)
public final class RenameFolderAction extends AbstractBookmarkAction {
    @Override
    public void perform(ActionContext context) {
        var folder = context.get(DataKeys.SELECTION, BookmarkStructure.Folder.class).orElseThrow();
        var name = promptName("Rename Folder", "Enter new name for folder '" + folder.name() + "':", folder.name());
        if (name != null) {
            bookmarks().updateFolder(folder.id(), name);
        }
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, BookmarkStructure.Folder.class)
            .filter(folder -> !folder.id().equals(bookmarks().rootFolderId()))
            .isPresent();
    }
}
