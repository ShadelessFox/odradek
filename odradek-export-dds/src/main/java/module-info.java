module odradek.export.dds {
    requires odradek.core;
    requires odradek.game;

    provides sh.adelessfox.odradek.game.Exporter with
        sh.adelessfox.odradek.export.dds.DdsExporter;
}
