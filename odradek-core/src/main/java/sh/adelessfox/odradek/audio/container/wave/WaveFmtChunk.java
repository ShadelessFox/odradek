package sh.adelessfox.odradek.audio.container.wave;

import sh.adelessfox.odradek.audio.container.riff.RiffChunk;
import sh.adelessfox.odradek.audio.container.riff.RiffChunkReader;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class WaveFmtChunk implements RiffChunk {
    public static final Id<WaveFmtChunk> ID = new Id<>("fmt ", WaveFmtChunk.class);

    public static final int WAVE_FORMAT_EXTENSIBLE = 0xFFFE;

    private final int formatTag;
    private final int channelCount;
    private final int sampleRate;
    private final int avgBytesPerSec;
    private final int blockAlign;
    private final int bitsPerSample;
    private final Object extension;

    public WaveFmtChunk(BinarySource source) throws IOException {
        formatTag = source.readUnsignedShort();
        channelCount = source.readUnsignedShort();
        sampleRate = source.readInt();
        avgBytesPerSec = source.readInt();
        blockAlign = source.readUnsignedShort();
        bitsPerSample = source.readUnsignedShort();
        extension = readExtension(source, formatTag);
    }

    public static RiffChunkReader<? extends WaveFmtChunk> reader() {
        return (source, _) -> new WaveFmtChunk(source);
    }

    public int formatTag() {
        return formatTag;
    }

    public int channelCount() {
        return channelCount;
    }

    public int sampleRate() {
        return sampleRate;
    }

    public int avgBytesPerSec() {
        return avgBytesPerSec;
    }

    public int blockAlign() {
        return blockAlign;
    }

    public int bitsPerSample() {
        return bitsPerSample;
    }

    public Object extension() {
        return extension;
    }

    protected Object readExtension(BinarySource source, int format) throws IOException {
        return null;
    }
}
