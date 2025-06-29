package sh.adelessfox.odradek.app.menu;

import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import static sh.adelessfox.odradek.app.menu.ActionIds.*;

@ActionRegistration(id = FILE_MENU_ID, text = "&File")
@ActionContribution(parent = MAIN_MENU_ID, order = 0)
public class FileMenu extends Action {
}
