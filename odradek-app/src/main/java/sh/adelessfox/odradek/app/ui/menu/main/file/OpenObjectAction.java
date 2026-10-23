package sh.adelessfox.odradek.app.ui.menu.main.file;

import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInputLazy;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

import javax.swing.*;

@ActionRegistration(text = "&Open Object\u2026", keystroke = "ctrl O")
@ActionContribution(parent = MainMenu.File.ID, order = 1)
public class OpenObjectAction extends Action {
    private static String lastInput;

    @Override
    public void perform(ActionContext context) {
        var game = context.require(DataKeys.GAME, DecimaGame.class);
        var editors = context.require(DataKeys.EDITOR_MANAGER);

        var result = (String) JOptionPane.showInputDialog(
            JOptionPane.getRootFrame(),
            "Enter [Group ID] : [Object Index]",
            "Open Object",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            lastInput);

        if (result == null) {
            return;
        }

        lastInput = result;
        editors.openEditor(new ObjectEditorInputLazy(game, ObjectId.valueOf(result.strip())));
    }
}
