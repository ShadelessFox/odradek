package sh.adelessfox.odradek.app.ui.menu.main.help;

import sh.adelessfox.odradek.app.ui.ApplicationDataKeys;
import sh.adelessfox.odradek.app.ui.component.onboarding.OnboardingDialog;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import javax.swing.*;

@ActionRegistration(text = "&Getting Started", description = "Learn how to browse, filter, inspect usages, and export resources")
@ActionContribution(parent = MainMenu.Help.ID, order = -100)
public final class GettingStartedAction extends Action {
    @Override
    public void perform(DataContext context) {
        OnboardingDialog.showGuide(JOptionPane.getRootFrame(), context.require(ApplicationDataKeys.SETTINGS));
    }
}
