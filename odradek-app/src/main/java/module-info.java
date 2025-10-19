module odradek.app {
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
    requires com.github.weisj.jsvg;
    requires com.miglayout.swing;
    requires dagger;
    requires info.picocli;
    requires jakarta.inject;
    requires java.desktop;
    requires odradek.core;
    requires odradek.game;
    requires odradek.game.hfw;
    requires odradek.rtti;
    requires odradek.ui;
    requires org.slf4j;

    // Runtime only
    requires odradek.export.dds;
    requires odradek.export.json;
    requires odradek.game.hfw.ui;
    requires odradek.viewer.model;
    requires odradek.viewer.texture;

    opens sh.adelessfox.odradek.app to info.picocli;

    provides sh.adelessfox.odradek.ui.actions.Action with
        sh.adelessfox.odradek.app.menu.main.MainMenu.File,
        sh.adelessfox.odradek.app.menu.main.MainMenu.Edit,
        sh.adelessfox.odradek.app.menu.main.MainMenu.Help,
        sh.adelessfox.odradek.app.menu.main.file.OpenObjectAction,
        sh.adelessfox.odradek.app.menu.main.help.AboutAction,
        sh.adelessfox.odradek.app.menu.graph.ExportObjectAction,
        sh.adelessfox.odradek.app.menu.graph.ExportObjectAction.Placeholder,
        sh.adelessfox.odradek.app.menu.graph.GroupObjectsByTypeAction,
        sh.adelessfox.odradek.app.menu.graph.SortObjectsByCountAction,
        sh.adelessfox.odradek.app.menu.object.CopyBytesToClipboardAction;

    provides sh.adelessfox.odradek.ui.editors.EditorProvider with
        sh.adelessfox.odradek.app.editors.ObjectEditorProvider;
}
