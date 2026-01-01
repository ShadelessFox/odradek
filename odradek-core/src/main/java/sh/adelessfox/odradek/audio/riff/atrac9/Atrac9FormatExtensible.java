package sh.adelessfox.odradek.audio.riff.atrac9;

import sh.adelessfox.odradek.audio.riff.wave.WaveFormatExtensible;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public final class Atrac9FormatExtensible extends WaveFormatExtensible {
    private final int versionInfo;
    private final byte[] configData;
    private final int reserved;

    public Atrac9FormatExtensible(BinaryReader reader) throws IOException {
        super(reader);

        versionInfo = reader.readInt();
        configData = reader.readBytes(4);
        reserved = reader.readInt();
    }

    public int versionInfo() {
        return versionInfo;
    }

    public byte[] configData() {
        return configData;
    }

    public int reserved() {
        return reserved;
    }
}
