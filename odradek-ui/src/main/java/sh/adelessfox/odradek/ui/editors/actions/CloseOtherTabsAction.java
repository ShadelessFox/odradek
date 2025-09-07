package sh.adelessfox.odradek.ui.editors.actions;

import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.Editor;

@ActionRegistration(text = "Close &Other Tabs")
@ActionContribution(parent = EditorActionIds.MENU_ID, order = 2000)
public class CloseOtherTabsAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var manager = context.get(DataKeys.EDITOR_MANAGER).orElseThrow();
        var editor = context.get(DataKeys.EDITOR).orElseThrow();
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();

        for (Editor other : manager.getEditors(stack)) {
            if (editor != other) {
                manager.closeEditor(other);
            }
        }
    }

    @Override
    public boolean isVisible(ActionContext context) {
        var manager = context.get(DataKeys.EDITOR_MANAGER).orElseThrow();
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        return manager.getEditors(stack).size() > 1;
    }
}
