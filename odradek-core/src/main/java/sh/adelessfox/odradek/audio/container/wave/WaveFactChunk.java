package sh.adelessfox.odradek.audio.container.wave;

import sh.adelessfox.odradek.audio.container.riff.RiffChunk;
import sh.adelessfox.odradek.audio.container.riff.RiffChunkReader;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class WaveFactChunk implements RiffChunk {
    public static final Id<WaveFactChunk> ID = new Id<>("fact", WaveFactChunk.class);

    private final int sampleCount;

    public WaveFactChunk(BinarySource source) throws IOException {
        sampleCount = source.readInt();
    }

    public static RiffChunkReader<? extends WaveFactChunk> reader() {
        return (source, _) -> new WaveFactChunk(source);
    }

    public int sampleCount() {
        return sampleCount;
    }
}
