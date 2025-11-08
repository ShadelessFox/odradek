module odradek.export.cast {
    requires odradek.core;
    requires odradek.game;
    requires be.twofold.tinycast;
    requires org.slf4j;

    provides sh.adelessfox.odradek.game.Exporter with
        sh.adelessfox.odradek.export.cast.CastExporter;
}
