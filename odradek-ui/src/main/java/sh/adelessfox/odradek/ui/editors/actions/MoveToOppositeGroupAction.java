package sh.adelessfox.odradek.ui.editors.actions;

import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "&Move to Opposite Group")
@ActionContribution(parent = EditorActionIds.MENU_ID, group = EditorActionIds.MENU_GROUP_SPLIT, order = 3000)
public class MoveToOppositeGroupAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var editor = context.get(DataKeys.EDITOR).orElseThrow();
        var source = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        var target = source.getOpposite().orElseThrow();
        source.move(editor, target);
    }

    @Override
    public boolean isVisible(ActionContext context) {
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        return stack.getOpposite().isPresent();
    }
}
