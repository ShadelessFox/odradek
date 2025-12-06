package sh.adelessfox.odradek.app.ui.menu.main.help;

import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import javax.swing.*;

@ActionRegistration(text = "&About")
@ActionContribution(parent = MainMenu.Help.ID)
public class AboutAction extends Action {
    @Override
    public void perform(ActionContext context) {
        JOptionPane.showMessageDialog(
            JOptionPane.getRootFrame(),
            "A Horizon Forbidden West asset viewer and extractor.\n\n© 2025 ShadelessFox",
            "About",
            JOptionPane.PLAIN_MESSAGE
        );
    }
}
