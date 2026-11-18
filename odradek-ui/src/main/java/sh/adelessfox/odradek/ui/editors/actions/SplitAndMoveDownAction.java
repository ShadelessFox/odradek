package sh.adelessfox.odradek.ui.editors.actions;

import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.stack.EditorStack;

@ActionRegistration(text = "Split and Move Down", icon = "fugue:layout-split-vertical")
@ActionContribution(parent = EditorMenu.ID, group = EditorMenu.GROUP_SPLIT, order = 2000)
public class SplitAndMoveDownAction extends Action {
    @Override
    public void perform(DataContext context) {
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        var editor = context.get(DataKeys.EDITOR).orElseThrow();
        stack.move(editor, stack, EditorStack.Position.BOTTOM);
    }

    @Override
    public boolean isVisible(DataContext context) {
        var manager = context.get(DataKeys.EDITOR_MANAGER).orElseThrow();
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        return manager.getEditors(stack).size() > 1;
    }
}
