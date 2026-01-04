module odradek.viewer.shader {
    requires java.desktop;
    requires odradek.core;
    requires odradek.ui;
    requires org.slf4j;
    requires odradek.game;

    provides sh.adelessfox.odradek.ui.Viewer.Provider with
        sh.adelessfox.odradek.viewer.shader.ShaderViewer.Provider;
}
