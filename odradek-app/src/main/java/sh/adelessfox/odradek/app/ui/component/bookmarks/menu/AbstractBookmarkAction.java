package sh.adelessfox.odradek.app.ui.component.bookmarks.menu;

import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.util.Gatherers;

import javax.swing.*;
import java.util.Collection;
import java.util.stream.Stream;

abstract class AbstractBookmarkAction extends Action {
    protected static String promptName(ObjectId id, String name) {
        while (true) {
            name = (String) JOptionPane.showInputDialog(
                JOptionPane.getRootFrame(),
                "Enter bookmark name for " + id + ":",
                "New Bookmark",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                name
            );
            if (name != null && name.isBlank()) {
                JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "Bookmark name cannot be empty",
                    "New Bookmark",
                    JOptionPane.ERROR_MESSAGE);
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
