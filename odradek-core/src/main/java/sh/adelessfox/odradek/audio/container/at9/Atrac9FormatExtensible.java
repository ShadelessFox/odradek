package sh.adelessfox.odradek.audio.container.at9;

import sh.adelessfox.odradek.audio.container.wave.WaveFormatExtensible;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public final class Atrac9FormatExtensible extends WaveFormatExtensible {
    private final int versionInfo;
    private final Bytes configData;
    private final int reserved;

    public Atrac9FormatExtensible(BinarySource source) throws IOException {
        super(source);

        versionInfo = source.readInt();
        configData = source.readBytes(4);
        reserved = source.readInt();
    }

    public int versionInfo() {
        return versionInfo;
    }

    public Bytes configData() {
        return configData;
    }

    public int reserved() {
        return reserved;
    }
}
