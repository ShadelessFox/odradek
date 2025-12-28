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
    requires odradek.game;
    requires odradek.game.hfw;
    requires odradek.game.hfw.ui;
    requires odradek.rtti;
    requires odradek.ui;
    requires org.slf4j;

    // Exporters
    requires odradek.export.cast;
    requires odradek.export.dds;
    requires odradek.export.json;

    // Viewers
    requires odradek.viewer.model;
    requires odradek.viewer.shader;
    requires odradek.viewer.texture;

    opens sh.adelessfox.odradek.app to info.picocli;
    opens sh.adelessfox.odradek.app.cli to info.picocli;
    opens sh.adelessfox.odradek.app.ui to info.picocli;
    opens sh.adelessfox.odradek.app.ui.bookmarks to com.google.gson;
    opens sh.adelessfox.odradek.app.ui.settings to com.google.gson;

    provides sh.adelessfox.odradek.ui.actions.Action with
        sh.adelessfox.odradek.app.ui.component.bookmarks.menu.ToggleBookmarkAction,
        sh.adelessfox.odradek.app.ui.menu.main.MainMenu.File,
        sh.adelessfox.odradek.app.ui.menu.main.MainMenu.Edit,
        sh.adelessfox.odradek.app.ui.menu.main.MainMenu.Help,
        sh.adelessfox.odradek.app.ui.menu.main.file.OpenObjectAction,
        sh.adelessfox.odradek.app.ui.menu.main.help.ReportAnIssueAction,
        sh.adelessfox.odradek.app.ui.menu.main.help.AboutAction,
        sh.adelessfox.odradek.app.ui.menu.graph.ExportObjectAction,
        sh.adelessfox.odradek.app.ui.menu.graph.ExportObjectAction.Placeholder,
        sh.adelessfox.odradek.app.ui.menu.graph.GroupObjectsByTypeAction,
        sh.adelessfox.odradek.app.ui.menu.graph.SortObjectsByCountAction,
        sh.adelessfox.odradek.app.ui.menu.object.CopyBytesToClipboardAction,
        sh.adelessfox.odradek.app.ui.menu.object.CopyIdToClipboardAction;

    provides sh.adelessfox.odradek.ui.editors.EditorProvider with
        sh.adelessfox.odradek.app.ui.editors.ObjectEditorProvider;
}
