module odradek.viewer.model {
    requires com.formdev.flatlaf;
    requires com.google.gson;
    requires com.miglayout.swing;
    requires java.desktop;
    requires odradek.core;
    requires odradek.game;
    requires odradek.opengl.awt;
    requires odradek.opengl;
    requires odradek.ui;
    requires org.slf4j;
    requires com.formdev.flatlaf.extras;

    opens sh.adelessfox.odradek.viewer.model.viewport.renderpass to com.google.gson;

    provides sh.adelessfox.odradek.ui.Viewer.Provider with
        sh.adelessfox.odradek.viewer.model.SceneViewer.Provider;
}
