module odradek.export.json {
    requires com.google.gson;
    requires odradek.game;
    requires odradek.rtti;

    provides sh.adelessfox.odradek.game.Exporter with
        sh.adelessfox.odradek.export.json.JsonExporter;
}
