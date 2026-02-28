package sh.adelessfox.odradek.app.ui.component.links.menu;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.component.bookmarks.menu.BookmarkMenu;
import sh.adelessfox.odradek.app.ui.component.links.LinkEvent;
import sh.adelessfox.odradek.app.ui.menu.graph.GraphMenu;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Show &Usages", icon = "fugue:chain", keystroke = "alt F7")
@ActionContribution(parent = GraphMenu.ID)
@ActionContribution(parent = BookmarkMenu.ID)
public final class ShowLinksAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var holder = context.get(DataKeys.SELECTION, ObjectIdHolder.class).orElseThrow();
        Application.getInstance().events().publish(new LinkEvent.ShowFor(holder.objectId()));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, ObjectIdHolder.class).isPresent();
    }
}
