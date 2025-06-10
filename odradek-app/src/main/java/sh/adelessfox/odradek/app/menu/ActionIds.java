package sh.adelessfox.odradek.app.menu;

public final class ActionIds {
    // @formatter:off
    public static final String MAIN_MENU_ID = "MainMenu";

    public static final String FILE_MENU_ID = MAIN_MENU_ID + ".File";
    public static final String EDIT_MENU_ID = MAIN_MENU_ID + ".Edit";
    public static final String HELP_MENU_ID = MAIN_MENU_ID + ".Help";

    public static final String APP_MENU_GROUP_MAIN    = "1000," + MAIN_MENU_ID + ".main";


    public static final String APP_MENU_HELP_GROUP_HELP = "1000," + HELP_MENU_ID + ".help";

    private ActionIds() {
    }
    // @formatter:on
}
