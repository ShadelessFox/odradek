module odradek.viewer.texture {
    requires java.desktop;
    requires odradek.ui;
    requires odradek.core;

    provides sh.adelessfox.odradek.ui.Viewer with
        sh.adelessfox.viewer.texture.TextureViewer;
}
