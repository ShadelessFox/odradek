module odradek.export.wave {
    requires odradek.core;
    requires odradek.game;

    provides sh.adelessfox.odradek.game.Exporter with
        sh.adelessfox.odradek.export.wave.WaveExporter;
}
