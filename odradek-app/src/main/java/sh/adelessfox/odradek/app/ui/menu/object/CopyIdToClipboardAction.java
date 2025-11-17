package sh.adelessfox.odradek.app.ui.menu.object;

import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInput;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.actions.EditorActionIds;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

@ActionRegistration(text = "Copy Object &ID", icon = "fugue:blue-document-copy")
@ActionContribution(parent = EditorActionIds.MENU_ID, group = EditorActionIds.MENU_GROUP_GENERAL)
public class CopyIdToClipboardAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var input = context.get(DataKeys.EDITOR)
            .map(e -> (ObjectEditorInput) e.getInput())
            .orElseThrow();

        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        var contents = new StringSelection(input.groupId() + ":" + input.objectIndex());
        clipboard.setContents(contents, contents);
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.EDITOR)
            .filter(e -> e.getInput() instanceof ObjectEditorInput)
            .isPresent();
    }
}
