module odradek.viewer.texture {
    requires com.miglayout.swing;
    requires java.desktop;
    requires odradek.core;
    requires odradek.game;
    requires odradek.rtti;
    requires odradek.ui;
    requires org.slf4j;

    provides sh.adelessfox.odradek.ui.Viewer.Provider with
        sh.adelessfox.odradek.viewer.texture.TextureViewer.Provider;

    provides sh.adelessfox.odradek.ui.Preview.Provider with
        sh.adelessfox.odradek.viewer.texture.TexturePreview.Provider;
}
