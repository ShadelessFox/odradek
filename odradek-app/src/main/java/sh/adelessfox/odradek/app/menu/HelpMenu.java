package sh.adelessfox.odradek.app.menu;

import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import static sh.adelessfox.odradek.app.menu.ActionIds.*;

@ActionRegistration(id = HELP_MENU_ID, text = "&Help")
@ActionContribution(parent = MAIN_MENU_ID, order = 2)
public class HelpMenu extends Action {
}
