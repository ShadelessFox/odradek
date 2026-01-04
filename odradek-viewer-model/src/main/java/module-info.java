module odradek.viewer.model {
    requires com.google.gson;
    requires java.desktop;
    requires odradek.core;
    requires odradek.opengl.awt;
    requires odradek.opengl;
    requires odradek.ui;
    requires org.slf4j;
    requires odradek.game;

    opens sh.adelessfox.odradek.viewer.model.viewport.renderpass to com.google.gson;

    provides sh.adelessfox.odradek.ui.Viewer.Provider with
        sh.adelessfox.odradek.viewer.model.SceneViewer.Provider;
}
