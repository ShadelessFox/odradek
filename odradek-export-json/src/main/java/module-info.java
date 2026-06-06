module odradek.export.json {
    requires transitive com.google.gson;
    requires odradek.core;
    requires odradek.game;
    requires odradek.rtti;

    exports sh.adelessfox.odradek.export.json.spi;

    provides sh.adelessfox.odradek.game.Exporter with
        sh.adelessfox.odradek.export.json.JsonExporter;

    uses sh.adelessfox.odradek.export.json.spi.TypeInfoAdapterFactory;
}
