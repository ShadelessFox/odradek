import sh.adelessfox.odradek.app.menu.EditMenu;
import sh.adelessfox.odradek.app.menu.FileMenu;
import sh.adelessfox.odradek.app.menu.HelpMenu;

module odradek.app {
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
    requires com.github.weisj.jsvg;
    requires com.miglayout.swing;
    requires info.picocli;
    requires java.desktop;
    requires odradek.core;
    requires odradek.game.hfw;
    requires odradek.rtti;
    requires odradek.ui;
    requires odradek.viewer.model;
    requires odradek.viewer.texture;
    requires org.slf4j;

    opens sh.adelessfox.odradek.app to info.picocli;

    provides sh.adelessfox.odradek.ui.actions.Action with
        FileMenu,
        EditMenu,
        HelpMenu,
        sh.adelessfox.odradek.app.menu.actions.AboutAction;
}
