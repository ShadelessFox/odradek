package sh.adelessfox.odradek.app.ui.menu.graph;

import sh.adelessfox.odradek.app.ui.component.bookmarks.menu.BookmarkMenu;
import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.actions.EditorMenu;
import sh.adelessfox.odradek.util.Gatherers;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Collection;
import java.util.stream.Collectors;

@ActionRegistration(text = "Copy Object &ID", icon = "fugue:blue-document-copy", keystroke = "ctrl shift C")
@ActionContribution(parent = GraphMenu.ID, group = MenuIds.GROUP_UTIL)
@ActionContribution(parent = EditorMenu.ID, group = MenuIds.GROUP_UTIL)
@ActionContribution(parent = BookmarkMenu.ID)
public class CopyIdToClipboardAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var ids = context.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(ObjectIdHolder.class))
            .map(ObjectIdHolder::objectId)
            .map(ObjectId::toString)
            .collect(Collectors.joining(", "));

        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        var contents = new StringSelection(ids);
        clipboard.setContents(contents, contents);
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .anyMatch(ObjectIdHolder.class::isInstance);
    }
}
