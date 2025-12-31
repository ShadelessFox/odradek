module odradek.viewer.audio {
    requires odradek.core;
    requires odradek.ui;
    requires org.slf4j;
    requires java.desktop;

    provides sh.adelessfox.odradek.ui.Viewer with
        sh.adelessfox.odradek.viewer.audio.AudioViewer;
}
