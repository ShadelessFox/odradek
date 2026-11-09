module odradek.export.bundle {
    requires odradek.core;
    requires odradek.game;
    requires org.slf4j;

    provides sh.adelessfox.odradek.game.Exporter with
        sh.adelessfox.odradek.export.bundle.ModelBundleDdsExporter,
        sh.adelessfox.odradek.export.bundle.ModelBundlePngExporter;
}
