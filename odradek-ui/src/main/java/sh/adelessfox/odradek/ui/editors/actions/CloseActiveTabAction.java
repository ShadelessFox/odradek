package sh.adelessfox.odradek.ui.editors.actions;

import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "&Close", keystroke = "ctrl F4")
@ActionContribution(parent = EditorActionIds.MENU_ID, order = 1000)
public class CloseActiveTabAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var manager = context.get(DataKeys.EDITOR_MANAGER).orElseThrow();
        var editor = context.get(DataKeys.EDITOR).orElseThrow();
        manager.closeEditor(editor);
    }
}
