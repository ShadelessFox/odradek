module odradek.export.dds {
    requires odradek.core;

    provides sh.adelessfox.odradek.export.Exporter with
        sh.adelessfox.odradek.export.dds.DdsExporter;
}
