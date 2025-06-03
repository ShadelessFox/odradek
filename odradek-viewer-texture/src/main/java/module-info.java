module odradek.viewer.texture {
    requires java.desktop;
    requires odradek.ui;
    requires odradek.core;
    requires org.slf4j;

    provides sh.adelessfox.odradek.ui.Viewer with
        sh.adelessfox.viewer.texture.TextureViewer;
}
