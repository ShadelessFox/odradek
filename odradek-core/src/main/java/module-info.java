module odradek.core {
    requires be.twofold.tinybcdec;
    requires org.lz4.java;
    requires org.slf4j;

    exports sh.adelessfox.odradek.audio.libatrac9;
    exports sh.adelessfox.odradek.audio.riff.atrac9;
    exports sh.adelessfox.odradek.audio.riff.wave;
    exports sh.adelessfox.odradek.audio.riff;
    exports sh.adelessfox.odradek.audio;
    exports sh.adelessfox.odradek.compression;
    exports sh.adelessfox.odradek.event;
    exports sh.adelessfox.odradek.geometry;
    exports sh.adelessfox.odradek.graphics;
    exports sh.adelessfox.odradek.hashing;
    exports sh.adelessfox.odradek.io;
    exports sh.adelessfox.odradek.math;
    exports sh.adelessfox.odradek.scene;
    exports sh.adelessfox.odradek.texture;
    exports sh.adelessfox.odradek.util;
    exports sh.adelessfox.odradek;
}
