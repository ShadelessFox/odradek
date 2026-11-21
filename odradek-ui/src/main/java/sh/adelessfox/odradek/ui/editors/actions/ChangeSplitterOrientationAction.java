package sh.adelessfox.odradek.ui.editors.actions;

import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.components.Splitter;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Change Orientation")
@ActionContribution(parent = EditorMenu.ID, group = EditorMenu.GROUP_SPLIT, order = 4000)
public class ChangeSplitterOrientationAction extends Action {
    @Override
    public void perform(DataContext context) {
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        var container = stack.getContainer().getSplitContainer();
        switch (container.getOrientation()) {
            case VERTICAL -> container.setOrientation(Splitter.Orientation.HORIZONTAL);
            case HORIZONTAL -> container.setOrientation(Splitter.Orientation.VERTICAL);
        }
    }

    @Override
    public boolean isVisible(DataContext context) {
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        return stack.getOpposite().isPresent();
    }
}
