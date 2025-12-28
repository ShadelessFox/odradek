package sh.adelessfox.odradek.app.ui.component.bookmarks.menu;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import java.util.Optional;

@ActionRegistration(text = "Rename Bookmark\u2026", icon = "fugue:bookmark--pencil", keystroke = "F2")
@ActionContribution(parent = BookmarkMenu.ID, order = 0)
public class RenameBookmarkAction extends AbstractBookmarkAction {
    @Override
    public void perform(ActionContext context) {
        var bookmarks = Application.getInstance().bookmarks();
        var bookmark = objects(context)
            .map(bookmarks::get).flatMap(Optional::stream)
            .findFirst();
        bookmark.ifPresent(b -> {
            var name = promptName(b.objectId(), b.name());
            if (name != null) {
                bookmarks.update(b.objectId(), name);
            }
        });
    }

    @Override
    public boolean isVisible(ActionContext context) {
        var bookmarks = Application.getInstance().bookmarks();
        return objects(context)
            .map(bookmarks::get)
            .map(Optional::isPresent)
            .limit(2).count() == 1;
    }
}
