package sh.adelessfox.odradek.app.ui.tools.bookmarks.menu;

import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.app.ui.tools.graph.menu.GraphMenu;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.editors.actions.EditorMenu;

import java.util.Optional;

@ActionRegistration(text = "Toggle Bookmark", icon = "fugue:blue-document-bookmark")
@ActionContribution(parent = GraphMenu.ID, group = MenuIds.GROUP_UTIL, order = 101)
@ActionContribution(parent = EditorMenu.ID, group = MenuIds.GROUP_UTIL)
@ActionContribution(parent = BookmarkMenu.ID, group = MenuIds.GROUP_UTIL, order = 1)
public class ToggleBookmarkAction extends AbstractBookmarkAction {
    @Override
    public void perform(ActionContext context) {
        var bookmarks = bookmarks();
        selectedKeys(context).forEach(key -> {
            if (bookmarks.get(key).isEmpty()) {
                var name = promptName("New Bookmark", "Enter name for " + key + ":", "New bookmark");
                if (name != null) {
                    bookmarks.create(bookmarks.rootFolderId(), key, name);
                }
            } else {
                bookmarks.delete(key);
            }
        });
    }

    @Override
    public boolean isVisible(ActionContext context) {
        var bookmarks = bookmarks();
        // Ensure we either have all bookmarked or none bookmarked
        return selectedKeys(context)
            .map(bookmarks::get)
            .map(Optional::isPresent)
            .distinct().limit(2).count() == 1;
    }

    @Override
    public Optional<String> getText(ActionContext context) {
        return Optional.of(exists(context) ? "Delete Bookmark" : "Add Bookmark");
    }

    @Override
    public Optional<String> getIcon(ActionContext context) {
        return Optional.of(exists(context) ? "fugue:bookmark--minus" : "fugue:bookmark--plus");
    }

    private static boolean exists(ActionContext context) {
        var bookmarks = bookmarks();
        var present = selectedKeys(context)
            .map(bookmarks::get)
            .flatMap(Optional::stream)
            .findFirst();
        return present.isPresent();
    }
}
