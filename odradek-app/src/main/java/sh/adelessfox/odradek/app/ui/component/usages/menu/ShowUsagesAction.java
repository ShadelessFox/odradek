package sh.adelessfox.odradek.app.ui.component.usages.menu;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.component.bookmarks.menu.BookmarkMenu;
import sh.adelessfox.odradek.app.ui.component.main.MainEvent;
import sh.adelessfox.odradek.app.ui.component.main.MainView;
import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.app.ui.menu.graph.GraphMenu;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.actions.EditorMenu;
import sh.adelessfox.odradek.util.Gatherers;

import java.util.Collection;

@ActionRegistration(text = "Show &Usages", icon = "fugue:chain", keystroke = "alt F7")
@ActionContribution(parent = GraphMenu.ID, group = MenuIds.GROUP_MISC)
@ActionContribution(parent = EditorMenu.ID, group = MenuIds.GROUP_MISC)
@ActionContribution(parent = BookmarkMenu.ID)
public final class ShowUsagesAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var holder = context.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .map(ObjectIdHolder.class::cast)
            .findFirst().orElseThrow();

        var eventBus = Application.getInstance().events();
        eventBus.publish(new MainEvent.ShowPanel(MainView.USAGES_PANEL_ID));
        eventBus.publish(new MainEvent.ShowLinks(holder.objectId()));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(ObjectIdHolder.class))
            .limit(2).count() == 1;
    }
}
