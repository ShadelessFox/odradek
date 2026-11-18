package sh.adelessfox.odradek.ui.editors.actions;

import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.Editor;

import java.util.Optional;

@ActionRegistration(text = "Close &All Tabs")
@ActionContribution(parent = EditorMenu.ID, group = EditorMenu.GROUP_CLOSE, order = 3000)
public class CloseAllTabsAction extends Action {
    @Override
    public void perform(DataContext context) {
        var manager = context.get(DataKeys.EDITOR_MANAGER).orElseThrow();
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();

        for (Editor editor : manager.getEditors(stack)) {
            manager.closeEditor(editor);
        }
    }

    @Override
    public boolean isVisible(DataContext context) {
        var manager = context.get(DataKeys.EDITOR_MANAGER).orElseThrow();
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        return manager.getEditors(stack).size() > 1;
    }

    @Override
    public Optional<String> getText(DataContext context) {
        var stack = context.get(DataKeys.EDITOR_STACK).orElseThrow();
        if (stack.getOpposite().isPresent()) {
            return Optional.of("Close &All Tabs in Group");
        } else {
            return Optional.of("Close &All Tabs");
        }
    }
}
