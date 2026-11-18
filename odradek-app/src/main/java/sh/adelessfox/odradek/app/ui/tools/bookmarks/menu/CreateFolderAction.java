package sh.adelessfox.odradek.app.ui.tools.bookmarks.menu;

import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.actions.MenuIds;

@ActionRegistration(text = "New Folder\u2026", icon = "fugue:folder--plus", keystroke = "ctrl shift N")
@ActionContribution(parent = BookmarkMenu.ID, group = MenuIds.GROUP_MISC, order = 0)
public final class CreateFolderAction extends AbstractBookmarkAction {
    @Override
    public void perform(DataContext context) {
        var parent = enclosingFolder(context).orElseThrow();
        var name = promptName("New Folder", "Enter folder name:", "New folder");
        if (name != null) {
            bookmarks(context).createFolder(parent, name);
        }
    }

    @Override
    public boolean isVisible(DataContext context) {
        return enclosingFolder(context).isPresent();
    }
}
