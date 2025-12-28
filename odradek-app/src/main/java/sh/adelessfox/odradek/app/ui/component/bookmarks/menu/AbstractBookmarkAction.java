package sh.adelessfox.odradek.app.ui.component.bookmarks.menu;

import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.util.Gatherers;

import javax.swing.*;
import java.util.Collection;
import java.util.stream.Stream;

public class AbstractBookmarkAction extends Action {
    protected static String promptName(ObjectId id, String name) {
        while (true) {
            name = (String) JOptionPane.showInputDialog(
                null,
                "Enter bookmark name for " + id + ":",
                "New Bookmark",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                name
            );
            if (name != null && name.isBlank()) {
                JOptionPane.showMessageDialog(null, "Bookmark name cannot be empty", "New Bookmark", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            return name;
        }
    }

    protected static Stream<ObjectId> objects(ActionContext context) {
        return context.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(ObjectIdHolder.class))
            .map(ObjectIdHolder::objectId);
    }
}
