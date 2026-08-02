module odradek.app {
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
    requires com.github.weisj.jsvg;
    requires com.google.gson;
    requires com.miglayout.swing;
    requires dagger;
    requires info.picocli;
    requires jakarta.inject;
    requires java.desktop;
    requires odradek.core;
    requires odradek.game.decima;
    requires odradek.rtti;
    requires odradek.ui;
    requires org.slf4j;

    requires wtf.reversed.toolbox;

    // VirtualMachine.list()
    requires jdk.attach;

    // Game support
    requires odradek.game.ds2.ui;
    requires odradek.game.ds2;
    requires odradek.game.hfw.ui;
    requires odradek.game.hfw;

    // Exporters
    requires odradek.export.cast;
    requires odradek.export.dds;
    requires odradek.export.json;
    requires odradek.export.png;
    requires odradek.export.shader;
    requires odradek.export.wave;

    // Viewers
    requires odradek.viewer.audio;
    requires odradek.viewer.model;
    requires odradek.viewer.shader;
    requires odradek.viewer.texture;

    opens sh.adelessfox.odradek.app to info.picocli;
    opens sh.adelessfox.odradek.app.cli to info.picocli;
    opens sh.adelessfox.odradek.app.ui to info.picocli;
    opens sh.adelessfox.odradek.app.ui.bookmarks to com.google.gson;
    opens sh.adelessfox.odradek.app.ui.settings to com.google.gson;
    opens sh.adelessfox.odradek.app.ui.settings.gson to com.google.gson;

    provides sh.adelessfox.odradek.ui.actions.Action with
        sh.adelessfox.odradek.app.ui.menu.main.MainMenu.File,
        sh.adelessfox.odradek.app.ui.menu.main.MainMenu.Help,
        sh.adelessfox.odradek.app.ui.menu.main.MainMenu.View,
        sh.adelessfox.odradek.app.ui.menu.main.file.OpenGraphAction,
        sh.adelessfox.odradek.app.ui.menu.main.file.OpenObjectAction,
        sh.adelessfox.odradek.app.ui.menu.main.help.AboutAction,
        sh.adelessfox.odradek.app.ui.menu.main.help.ReportAnIssueAction,
        sh.adelessfox.odradek.app.ui.menu.main.view.ThemeAction,
        sh.adelessfox.odradek.app.ui.menu.main.view.ThemeAction.Placeholder,
        sh.adelessfox.odradek.app.ui.menu.main.view.ToggleShowObjectPreviewAction,
        sh.adelessfox.odradek.app.ui.menu.main.view.ToggleShowObjectTypeInformationAction,
        sh.adelessfox.odradek.app.ui.tools.bookmarks.menu.RenameBookmarkAction,
        sh.adelessfox.odradek.app.ui.tools.bookmarks.menu.ToggleBookmarkAction,
        sh.adelessfox.odradek.app.ui.tools.graph.menu.CopyIdToClipboardAction,
        sh.adelessfox.odradek.app.ui.tools.graph.menu.ExportObjectAction,
        sh.adelessfox.odradek.app.ui.tools.graph.menu.ExportObjectAction.Placeholder,
        sh.adelessfox.odradek.app.ui.tools.graph.menu.GroupObjectsByGroupAction,
        sh.adelessfox.odradek.app.ui.tools.graph.menu.GroupObjectsByTypeAction,
        sh.adelessfox.odradek.app.ui.tools.graph.menu.SortGroupsByCountAction,
        sh.adelessfox.odradek.app.ui.tools.graph.menu.SortObjectsByCountAction,
        sh.adelessfox.odradek.app.ui.tools.usages.menu.ShowUsagesAction,
        sh.adelessfox.odradek.app.ui.viewers.menu.CopyBytesToClipboardAction,
        sh.adelessfox.odradek.app.ui.viewers.menu.SaveBytesToFileAction;

    provides sh.adelessfox.odradek.ui.editors.Editor.Provider with
        sh.adelessfox.odradek.app.ui.editors.ObjectEditor.Provider;

    provides sh.adelessfox.odradek.ui.Viewer.Provider with
        sh.adelessfox.odradek.app.ui.viewers.ObjectViewer.Provider;

    provides sh.adelessfox.odradek.app.util.GameLocator
        with sh.adelessfox.odradek.app.util.steam.SteamGameLocator;

    uses sh.adelessfox.odradek.app.util.GameLocator;
}
