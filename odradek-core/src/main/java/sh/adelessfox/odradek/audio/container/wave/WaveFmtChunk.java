package sh.adelessfox.odradek.audio.container.wave;

import sh.adelessfox.odradek.audio.container.riff.RiffChunk;
import sh.adelessfox.odradek.audio.container.riff.RiffChunkReader;
import sh.adelessfox.odradek.io.BinaryReader;

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
    private final WaveFormatExtensible extension;

    public WaveFmtChunk(BinaryReader reader) throws IOException {
        formatTag = reader.readShort() & 0xffff;
        channelCount = reader.readShort() & 0xffff;
        sampleRate = reader.readInt();
        avgBytesPerSec = reader.readInt();
        blockAlign = reader.readShort() & 0xffff;
        bitsPerSample = reader.readShort() & 0xffff;

        if (formatTag == WAVE_FORMAT_EXTENSIBLE) {
            extension = readExtension(reader);
        } else {
            extension = null;
        }
    }

    public static RiffChunkReader<? extends WaveFmtChunk> reader() {
        return (reader, _) -> new WaveFmtChunk(reader);
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

    public WaveFormatExtensible extension() {
        return extension;
    }

    protected WaveFormatExtensible readExtension(BinaryReader reader) throws IOException {
        return new WaveFormatExtensible(reader);
    }
}
