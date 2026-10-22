module odradek.ui.bookmarks {
    requires com.google.gson;
    requires java.desktop;
    requires odradek.core;
    requires odradek.game.decima;
    requires odradek.settings;
    requires odradek.ui;

    exports sh.adelessfox.odradek.ui.bookmarks;

    opens sh.adelessfox.odradek.ui.bookmarks to com.google.gson;
    opens sh.adelessfox.odradek.ui.bookmarks.gson to com.google.gson;

    provides sh.adelessfox.odradek.ui.actions.Action with
        sh.adelessfox.odradek.ui.bookmarks.internal.menu.CreateFolderAction,
        sh.adelessfox.odradek.ui.bookmarks.internal.menu.DeleteFolderAction,
        sh.adelessfox.odradek.ui.bookmarks.internal.menu.RenameBookmarkAction,
        sh.adelessfox.odradek.ui.bookmarks.internal.menu.RenameFolderAction,
        sh.adelessfox.odradek.ui.bookmarks.internal.menu.ToggleBookmarkAction;

    provides sh.adelessfox.odradek.ui.tools.ToolPanel.Provider with
        sh.adelessfox.odradek.ui.bookmarks.internal.BookmarkToolPanel.Provider;
}
