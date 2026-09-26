package sh.adelessfox.odradek.app.ui.viewers.menu;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInput;
import sh.adelessfox.odradek.app.ui.viewers.ObjectStructure;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Open in new tab")
@ActionContribution(parent = ObjectMenu.ID, group = "100,Navigation")
public final class OpenLinkInNewTabAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var node = context.get(DataKeys.SELECTION, ObjectStructure.Node.class).orElseThrow();
        ObjectEditorInput.fromLink(node.game(), node.value())
            .ifPresent(input -> Application.getInstance().editors().openEditorInNewTab(input));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, ObjectStructure.Node.class)
            .filter(node -> node.value() instanceof ObjectIdHolder)
            .isPresent();
    }
}
