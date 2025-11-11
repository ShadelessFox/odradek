package sh.adelessfox.odradek.ui.editors.actions;

public final class EditorActionIds {
    /** An identifier of the context menu shown when right-clicking on an editor's tab. */
    public static final String MENU_ID = "EditorsMenu";

    public static final String MENU_GROUP_CLOSE = "1000," + MENU_ID + ".close";
    public static final String MENU_GROUP_SPLIT = "2000," + MENU_ID + ".split";
    public static final String MENU_GROUP_GENERAL = "3000," + MENU_ID + ".general";

    private EditorActionIds() {
    }
}
