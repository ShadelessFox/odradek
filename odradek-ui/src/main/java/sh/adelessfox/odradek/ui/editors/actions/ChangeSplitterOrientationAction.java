package sh.adelessfox.odradek.ui.editors.actions;

import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackContainer;

@ActionRegistration(text = "Change Orientation")
@ActionContribution(parent = EditorActionIds.MENU_ID, group = EditorActionIds.MENU_GROUP_SPLIT, order = 4000)
public class ChangeSplitterOrientationAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        var container = stack.getContainer().getSplitContainer();
        switch (container.getOrientation()) {
            case VERTICAL -> container.setOrientation(EditorStackContainer.Orientation.HORIZONTAL);
            case HORIZONTAL -> container.setOrientation(EditorStackContainer.Orientation.VERTICAL);
        }
    }

    @Override
    public boolean isVisible(ActionContext context) {
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        return stack.getOpposite().isPresent();
    }
}
