module odradek.viewer.texture {
    requires com.miglayout.swing;
    requires java.desktop;
    requires odradek.core;
    requires odradek.game;
    requires odradek.rtti;
    requires odradek.ui;
    requires org.slf4j;

    provides sh.adelessfox.odradek.ui.Viewer with
        sh.adelessfox.odradek.viewer.texture.TextureViewer;

    provides sh.adelessfox.odradek.ui.Preview with
        sh.adelessfox.odradek.viewer.texture.TexturePreview;
}
