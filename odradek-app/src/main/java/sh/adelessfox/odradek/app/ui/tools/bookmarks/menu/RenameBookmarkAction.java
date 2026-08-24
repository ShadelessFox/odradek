package sh.adelessfox.odradek.app.ui.tools.bookmarks.menu;

import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import java.util.Optional;

@ActionRegistration(text = "Rename Bookmark\u2026", icon = "fugue:bookmark--pencil")
@ActionContribution(parent = BookmarkMenu.ID, group = MenuIds.GROUP_UTIL, order = 0)
public class RenameBookmarkAction extends AbstractBookmarkAction {
    @Override
    public void perform(ActionContext context) {
        var bookmarks = bookmarks();
        var bookmark = selectedKeys(context)
            .map(bookmarks::get).flatMap(Optional::stream)
            .findFirst();
        bookmark.ifPresent(b -> {
            var name = promptName("New Bookmark", "Enter name for " + b.key() + ":", b.name());
            if (name != null) {
                bookmarks.update(b.key(), name);
            }
        });
    }

    @Override
    public boolean isVisible(ActionContext context) {
        var bookmarks = bookmarks();
        return selectedKeys(context)
            .map(bookmarks::get)
            .map(Optional::isPresent)
            .limit(2).count() == 1;
    }
}
