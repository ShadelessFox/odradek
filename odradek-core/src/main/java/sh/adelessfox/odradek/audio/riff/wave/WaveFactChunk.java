package sh.adelessfox.odradek.audio.riff.wave;

import sh.adelessfox.odradek.audio.riff.RiffChunk;
import sh.adelessfox.odradek.audio.riff.RiffChunkReader;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class WaveFactChunk implements RiffChunk {
    public static final Id<WaveFactChunk> ID = new Id<>("fact", WaveFactChunk.class);

    private final int sampleCount;

    public WaveFactChunk(BinaryReader reader) throws IOException {
        sampleCount = reader.readInt();
    }

    public static RiffChunkReader<? extends WaveFactChunk> reader() {
        return (reader, _) -> new WaveFactChunk(reader);
    }

    public int sampleCount() {
        return sampleCount;
    }
}
