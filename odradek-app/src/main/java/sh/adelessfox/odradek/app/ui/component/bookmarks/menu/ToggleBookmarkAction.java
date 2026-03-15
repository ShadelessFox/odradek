package sh.adelessfox.odradek.app.ui.component.bookmarks.menu;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.app.ui.menu.graph.GraphMenu;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.editors.actions.EditorMenu;

import java.util.Optional;

@ActionRegistration(text = "Toggle Bookmark", icon = "fugue:blue-document-bookmark")
@ActionContribution(parent = GraphMenu.ID, group = MenuIds.GROUP_UTIL)
@ActionContribution(parent = EditorMenu.ID, group = MenuIds.GROUP_UTIL)
@ActionContribution(parent = BookmarkMenu.ID)
public class ToggleBookmarkAction extends AbstractBookmarkAction {
    @Override
    public void perform(ActionContext context) {
        var bookmarks = Application.getInstance().bookmarks();
        objects(context).forEach(id -> {
            if (bookmarks.get(id).isEmpty()) {
                var name = promptName(id, "New bookmark");
                if (name != null) {
                    bookmarks.create(id, name);
                }
            } else {
                bookmarks.delete(id);
            }
        });
    }

    @Override
    public boolean isVisible(ActionContext context) {
        var bookmarks = Application.getInstance().bookmarks();
        // Ensure we either have all bookmarked or none bookmarked
        return objects(context)
            .map(bookmarks::get)
            .map(Optional::isPresent)
            .distinct().limit(2).count() == 1;
    }

    @Override
    public Optional<String> getText(ActionContext context) {
        return Optional.of(exists(context) ? "Remove Bookmark" : "Add Bookmark");
    }

    @Override
    public Optional<String> getIcon(ActionContext context) {
        return Optional.of(exists(context) ? "fugue:bookmark--minus" : "fugue:bookmark--plus");
    }

    private static boolean exists(ActionContext context) {
        var bookmarks = Application.getInstance().bookmarks();
        var present = objects(context)
            .map(bookmarks::get)
            .flatMap(Optional::stream)
            .findFirst();
        return present.isPresent();
    }
}
