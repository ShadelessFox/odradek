module odradek.core {
    requires be.twofold.tinybcdec;

    exports sh.adelessfox.odradek.compression;
    exports sh.adelessfox.odradek.export;
    exports sh.adelessfox.odradek.game;
    exports sh.adelessfox.odradek.geometry;
    exports sh.adelessfox.odradek.hashing;
    exports sh.adelessfox.odradek.io;
    exports sh.adelessfox.odradek.math;
    exports sh.adelessfox.odradek.scene;
    exports sh.adelessfox.odradek.texture;
    exports sh.adelessfox.odradek;

    uses sh.adelessfox.odradek.export.Exporter;
    uses sh.adelessfox.odradek.game.Converter;
}
