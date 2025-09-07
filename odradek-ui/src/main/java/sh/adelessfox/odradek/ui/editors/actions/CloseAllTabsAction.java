package sh.adelessfox.odradek.ui.editors.actions;

import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.Editor;

import java.util.Optional;

@ActionRegistration(text = "Close &All Tabs")
@ActionContribution(parent = EditorActionIds.MENU_ID, group = EditorActionIds.MENU_GROUP_CLOSE, order = 3000)
public class CloseAllTabsAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var manager = context.get(DataKeys.EDITOR_MANAGER).orElseThrow();
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();

        for (Editor editor : manager.getEditors(stack)) {
            manager.closeEditor(editor);
        }
    }

    @Override
    public boolean isVisible(ActionContext context) {
        var manager = context.get(DataKeys.EDITOR_MANAGER).orElseThrow();
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        return manager.getEditors(stack).size() > 1;
    }

    @Override
    public Optional<String> getText(ActionContext context) {
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        if (stack.getOpposite().isPresent()) {
            return Optional.of("Close &All Tabs in Group");
        } else {
            return Optional.of("Close &All Tabs");
        }
    }
}
