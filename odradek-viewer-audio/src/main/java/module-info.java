module odradek.viewer.audio {
    requires com.formdev.flatlaf;
    requires com.miglayout.swing;
    requires odradek.core;
    requires odradek.ui;
    requires org.slf4j;
    requires java.desktop;
    requires odradek.game;

    provides sh.adelessfox.odradek.ui.Viewer.Provider with
        sh.adelessfox.odradek.viewer.audio.AudioViewer.Provider;

    provides com.formdev.flatlaf.FlatDefaultsAddon with
        sh.adelessfox.odradek.viewer.audio.laf.AudioPlayerDefaultsAddon;

    opens sh.adelessfox.odradek.viewer.audio.laf;
}
