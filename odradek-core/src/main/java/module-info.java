module odradek.core {
    requires be.twofold.tinybcdec;
    requires org.lz4.java;
    requires org.slf4j;

    exports sh.adelessfox.odradek.audio.codec;
    exports sh.adelessfox.odradek.audio.container.at9;
    exports sh.adelessfox.odradek.audio.container.riff;
    exports sh.adelessfox.odradek.audio.container.wave;
    exports sh.adelessfox.odradek.audio.reader;
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
