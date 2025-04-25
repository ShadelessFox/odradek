module odradek.app {
    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
    requires com.miglayout.swing;
    requires java.desktop;
    requires lwjgl3.awt;
    requires odradek.core;
    requires odradek.game.hfw;
    requires odradek.rtti;
    requires org.lwjgl.natives;
    requires org.lwjgl.opengl.natives;
    requires org.lwjgl.opengl;
    requires org.lwjgl;
    requires org.slf4j;

    opens sh.adelessfox.odradek.app.ui.util;

    exports sh.adelessfox.odradek.app.ui.laf to com.formdev.flatlaf;

    provides com.formdev.flatlaf.FlatDefaultsAddon
        with sh.adelessfox.odradek.app.ApplicationDefaultsAddon;
}
