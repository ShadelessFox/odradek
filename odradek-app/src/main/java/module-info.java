module odradek.app {
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
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
}
