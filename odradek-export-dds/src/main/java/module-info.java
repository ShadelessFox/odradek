module odradek.export.dds {
    requires odradek.core;
    requires odradek.game;
    requires org.slf4j;

    provides sh.adelessfox.odradek.game.Exporter with
        sh.adelessfox.odradek.export.dds.TextureDdsExporter,
        sh.adelessfox.odradek.export.dds.TextureSetDdsExporter;
}
