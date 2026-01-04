package sh.adelessfox.odradek.audio.container.wave;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class WaveFormatExtensible {
    private final int size;
    private final int validBitsPerSample;
    private final int channelMask;
    private final byte[] subFormat;

    public WaveFormatExtensible(BinaryReader reader) throws IOException {
        size = Short.toUnsignedInt(reader.readShort());
        validBitsPerSample = Short.toUnsignedInt(reader.readShort());
        channelMask = reader.readInt();
        subFormat = reader.readBytes(16);
    }

    public int size() {
        return size;
    }

    public int validBitsPerSample() {
        return validBitsPerSample;
    }

    public int channelMask() {
        return channelMask;
    }

    public byte[] subFormat() {
        return subFormat;
    }
}
