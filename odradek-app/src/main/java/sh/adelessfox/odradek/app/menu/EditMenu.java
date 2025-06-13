package sh.adelessfox.odradek.app.menu;

import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import static sh.adelessfox.odradek.app.menu.ActionIds.*;

@ActionRegistration(id = EDIT_MENU_ID, name = "&Edit")
@ActionContribution(parent = MAIN_MENU_ID, order = 1)
public class EditMenu extends Action {
}
