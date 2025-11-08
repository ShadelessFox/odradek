package sh.adelessfox.odradek.app.ui.menu.main;

import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

public final class MainMenu {
    public static final String ID = "MainMenu";

    private MainMenu() {
    }

    @ActionRegistration(id = Edit.ID, text = "&Edit")
    @ActionContribution(parent = ID, order = 1)
    public static class Edit extends Action {
        public static final String ID = MainMenu.ID + ".Edit";
    }

    @ActionRegistration(id = File.ID, text = "&File")
    @ActionContribution(parent = ID, order = 0)
    public static class File extends Action {
        public static final String ID = MainMenu.ID + ".File";
    }

    @ActionRegistration(id = Help.ID, text = "&Help")
    @ActionContribution(parent = ID, order = 2)
    public static class Help extends Action {
        public static final String ID = MainMenu.ID + ".Help";
    }
}
