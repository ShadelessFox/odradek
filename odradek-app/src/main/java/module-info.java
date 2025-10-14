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
        sh.adelessfox.odradek.app.menu.EditMenu,
        sh.adelessfox.odradek.app.menu.FileMenu,
        sh.adelessfox.odradek.app.menu.HelpMenu,
        sh.adelessfox.odradek.app.menu.actions.file.OpenObjectAction,
        sh.adelessfox.odradek.app.menu.actions.help.AboutAction,
        sh.adelessfox.odradek.app.menu.actions.graph.ExportObjectAction,
        sh.adelessfox.odradek.app.menu.actions.graph.ExportObjectAction.Placeholder,
        sh.adelessfox.odradek.app.menu.actions.graph.GroupObjectsByTypeAction,
        sh.adelessfox.odradek.app.menu.actions.graph.SortObjectsByCountAction,
        sh.adelessfox.odradek.app.menu.actions.object.CopyBytesToClipboardAction,
        sh.adelessfox.odradek.app.menu.actions.tabs.CloseAllTabsAction;

    provides sh.adelessfox.odradek.ui.editors.EditorProvider with
        sh.adelessfox.odradek.app.editors.ObjectEditorProvider;
}
