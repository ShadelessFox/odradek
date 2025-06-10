package sh.adelessfox.odradek.app.menu.actions;

import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import javax.swing.*;

@ActionRegistration(id = "sh.adelessfox.odradek.app.menu.actions.AboutAction", name = "&About")
@ActionContribution(parent = ActionIds.HELP_MENU_ID, group = ActionIds.APP_MENU_HELP_GROUP_HELP)
public class AboutAction extends Action {
    @Override
    public void perform(ActionContext context) {
        JOptionPane.showMessageDialog(
            JOptionPane.getRootFrame(),
            "A Horizon Forbidden West asset viewer and extractor.",
            "About",
            JOptionPane.PLAIN_MESSAGE
        );
    }
}
