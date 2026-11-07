package sh.adelessfox.odradek.audio.container.wave;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class WaveFormatExtensible {
    private final int size;
    private final int validBitsPerSample;
    private final int channelMask;
    private final Bytes subFormat;

    public WaveFormatExtensible(BinarySource source) throws IOException {
        size = Short.toUnsignedInt(source.readShort());
        validBitsPerSample = Short.toUnsignedInt(source.readShort());
        channelMask = source.readInt();
        subFormat = source.readBytes(16);
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

    public Bytes subFormat() {
        return subFormat;
    }
}
