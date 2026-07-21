module odradek.core {
    requires static java.desktop; // sh.adelessfox.odradek.texture.Converter.AWT

    requires transitive wtf.reversed.toolbox;
    requires be.twofold.tinybcdec; // sh.adelessfox.odradek.texture.processing.TileUnpacker#decompress
    requires com.kichik.pecoff4j; // sh.adelessfox.odradek.util.system.ProductVersion
    requires java.net.http; // sh.adelessfox.odradek.audio.VgmstreamDownloader
    requires org.slf4j;

    exports sh.adelessfox.odradek.animation;
    exports sh.adelessfox.odradek.audio.container.at9;
    exports sh.adelessfox.odradek.audio.container.riff;
    exports sh.adelessfox.odradek.audio.container.wave;
    exports sh.adelessfox.odradek.audio.container.wwise;
    exports sh.adelessfox.odradek.audio;
    exports sh.adelessfox.odradek.event;
    exports sh.adelessfox.odradek.geometry;
    exports sh.adelessfox.odradek.graphics;
    exports sh.adelessfox.odradek.io;
    exports sh.adelessfox.odradek.math;
    exports sh.adelessfox.odradek.parsing.util;
    exports sh.adelessfox.odradek.parsing;
    exports sh.adelessfox.odradek.scene;
    exports sh.adelessfox.odradek.texture;
    exports sh.adelessfox.odradek.util.system;
    exports sh.adelessfox.odradek.util;
    exports sh.adelessfox.odradek;
}
