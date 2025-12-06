package sh.adelessfox.odradek.app.ui.menu.main.help;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

@ActionRegistration(text = "&Report an Issue\u2026")
@ActionContribution(parent = MainMenu.Help.ID)
public class ReportAnIssueAction extends Action {
    private static final Logger log = LoggerFactory.getLogger(ReportAnIssueAction.class);

    @Override
    public void perform(ActionContext context) {
        try {
            Desktop.getDesktop().browse(URI.create("https://github.com/ShadelessFox/odradek/issues/new"));
        } catch (IOException e) {
            log.error("Unable to open the link", e);
        }
    }
}
