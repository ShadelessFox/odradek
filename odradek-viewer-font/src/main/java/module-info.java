module odradek.viewer.font {
    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
    requires com.miglayout.swing;
    requires java.desktop;
    requires odradek.core;
    requires odradek.game;
    requires odradek.ui;

    provides sh.adelessfox.odradek.ui.Viewer.Provider with
        sh.adelessfox.odradek.viewer.font.FontViewer.Provider;
}
