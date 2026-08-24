package sh.adelessfox.odradek.app.ui.tools.bookmarks.menu;

import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

@ActionRegistration(text = "New Folder\u2026", icon = "fugue:folder--plus", keystroke = "ctrl shift N")
@ActionContribution(parent = BookmarkMenu.ID, group = MenuIds.GROUP_MISC, order = 0)
public final class CreateFolderAction extends AbstractBookmarkAction {
    @Override
    public void perform(ActionContext context) {
        var parent = enclosingFolder(context).orElseThrow();
        var name = promptName("New Folder", "Enter folder name:", "New folder");
        if (name != null) {
            bookmarks().createFolder(parent, name);
        }
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return enclosingFolder(context).isPresent();
    }
}
