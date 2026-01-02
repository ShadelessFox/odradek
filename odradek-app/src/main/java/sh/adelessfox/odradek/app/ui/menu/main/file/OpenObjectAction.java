package sh.adelessfox.odradek.app.ui.menu.main.file;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInputLazy;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import javax.swing.*;

@ActionRegistration(text = "&Open Object\u2026", keystroke = "ctrl O")
@ActionContribution(parent = MainMenu.File.ID)
public class OpenObjectAction extends Action {
    private static String lastInput;

    @Override
    public void perform(ActionContext context) {
        String result = (String) JOptionPane.showInputDialog(
            JOptionPane.getRootFrame(),
            "Enter [Group ID] : [Object Index]",
            "Open Object",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            lastInput
        );

        if (result == null) {
            return;
        }

        lastInput = result;

        int colon = result.indexOf(':');
        int groupId = Integer.parseUnsignedInt(result.substring(0, colon).strip());
        int objectIndex = Integer.parseUnsignedInt(result.substring(colon + 1).strip());

        Application.getInstance().editors().openEditor(new ObjectEditorInputLazy(groupId, objectIndex));
    }
}
