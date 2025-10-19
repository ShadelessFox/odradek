package sh.adelessfox.odradek.app.menu.main.file;

import sh.adelessfox.odradek.app.ApplicationKeys;
import sh.adelessfox.odradek.app.menu.main.MainMenu;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import javax.swing.*;

@ActionRegistration(text = "&Open Object\u2026", keystroke = "ctrl O")
@ActionContribution(parent = MainMenu.File.ID)
public class OpenObjectAction extends Action {
    @Override
    public void perform(ActionContext context) {
        String result = JOptionPane.showInputDialog(
            JOptionPane.getRootFrame(),
            "Enter [Group ID] : [Object Index]",
            "Open Object",
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == null) {
            return;
        }

        int colon = result.indexOf(':');
        int groupId = Integer.parseUnsignedInt(result.substring(0, colon).strip());
        int objectIndex = Integer.parseUnsignedInt(result.substring(colon + 1).strip());

        context.get(ApplicationKeys.MAIN_PRESENTER).ifPresent(presenter -> presenter.showObject(groupId, objectIndex));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.has(ApplicationKeys.MAIN_PRESENTER);
    }
}
