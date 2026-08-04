package sh.adelessfox.odradek.app.ui.menu.main.help;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import sh.adelessfox.odradek.app.ui.Application;
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
            "An asset viewer/extractor for Horizon Forbidden\nWest and Death Stranding 2.\n\n© 2025-2026 ShadelessFox and contributors",
            "About",
            JOptionPane.PLAIN_MESSAGE,
            new FlatSVGIcon(Application.class.getResource("application.svg")).derive(64, 64));
    }
}
