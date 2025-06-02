module odradek.app {
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
    requires com.google.gson;
    requires com.miglayout.swing;
    requires info.picocli;
    requires java.desktop;
    requires odradek.core;
    requires odradek.game.hfw;
    requires odradek.opengl.awt;
    requires odradek.opengl;
    requires odradek.rtti;
    requires odradek.ui;
    requires org.slf4j;

    opens sh.adelessfox.odradek.app to info.picocli;
    opens sh.adelessfox.odradek.app.viewport.renderpass to com.google.gson;
}
