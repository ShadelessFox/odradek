package sh.adelessfox.odradek.app.ui.component.bookmarks.menu;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.menu.graph.GraphMenu;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.actions.EditorActionIds;
import sh.adelessfox.odradek.util.Gatherers;

import javax.swing.*;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@ActionRegistration(text = "Toggle Bookmark", icon = "fugue:blue-document-bookmark")
@ActionContribution(parent = GraphMenu.ID)
@ActionContribution(parent = BookmarkMenu.ID)
@ActionContribution(parent = EditorActionIds.MENU_ID, group = EditorActionIds.MENU_GROUP_GENERAL)
public class ToggleBookmarkAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var bookmarks = Application.getInstance().bookmarks();
        objects(context).forEach(id -> {
            if (bookmarks.get(id).isEmpty()) {
                var name = promptName(id);
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
        var bookmarks = Application.getInstance().bookmarks();
        var present = objects(context)
            .map(bookmarks::get)
            .flatMap(Optional::stream)
            .findFirst();
        return Optional.of(present.isPresent() ? "Remove Bookmark" : "Add Bookmark");
    }

    private static Stream<ObjectId> objects(ActionContext context) {
        return context.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(ObjectIdHolder.class))
            .map(ObjectIdHolder::objectId);
    }

    private static String promptName(ObjectId id) {
        while (true) {
            String name = (String) JOptionPane.showInputDialog(
                null,
                "Enter bookmark name for " + id + ":",
                "New Bookmark",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "New bookmark"
            );
            if (name != null && name.isBlank()) {
                JOptionPane.showMessageDialog(null, "Bookmark name cannot be empty", "New Bookmark", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            return name;
        }
    }
}
