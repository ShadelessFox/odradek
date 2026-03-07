module odradek.export.png {
    requires java.desktop;
    requires odradek.core;
    requires odradek.game;
    requires odradek.rtti;
    requires org.slf4j;

    provides sh.adelessfox.odradek.game.Exporter with
        sh.adelessfox.odradek.export.png.PngExporter;
}
