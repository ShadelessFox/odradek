package sh.adelessfox.odradek.app.ui.tools.usages.menu;

import sh.adelessfox.odradek.app.ui.component.main.MainEvent;
import sh.adelessfox.odradek.app.ui.tools.bookmarks.menu.BookmarkMenu;
import sh.adelessfox.odradek.app.ui.tools.graph.GraphMenu;
import sh.adelessfox.odradek.app.ui.tools.usages.UsagesToolPanel;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.*;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.actions.EditorMenu;

@ActionRegistration(text = "Show &Usages", icon = "fugue:chain", keystroke = "alt F7")
@ActionContribution(parent = GraphMenu.ID, group = MenuIds.GROUP_MISC)
@ActionContribution(parent = EditorMenu.ID, group = MenuIds.GROUP_MISC)
@ActionContribution(parent = BookmarkMenu.ID)
public final class ShowUsagesAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var holder = context.require(DataKeys.SELECTION, ObjectIdHolder.class);
        var eventBus = context.require(DataKeys.EVENT_BUS);
        eventBus.publish(new MainEvent.ShowPanel(UsagesToolPanel.ID, true));
        eventBus.publish(new MainEvent.ShowUsages(holder.objectId()));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, ObjectIdHolder.class).isPresent();
    }
}
