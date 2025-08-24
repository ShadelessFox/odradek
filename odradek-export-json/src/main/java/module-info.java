module odradek.export.json {
    requires com.google.gson;
    requires odradek.core;
    requires odradek.rtti;

    provides sh.adelessfox.odradek.export.Exporter with
        sh.adelessfox.odradek.export.json.JsonExporter;
}
