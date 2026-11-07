package sh.adelessfox.odradek.audio.container.at9;

import sh.adelessfox.odradek.audio.container.riff.RiffChunkReader;
import sh.adelessfox.odradek.audio.container.wave.WaveFactChunk;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public final class Atrac9FactChunk extends WaveFactChunk {
    public static final Id<Atrac9FactChunk> ID = new Id<>("fact", Atrac9FactChunk.class);

    private final int inputOverlapDelaySamples;
    private final int encoderDelaySamples;

    public Atrac9FactChunk(BinarySource source) throws IOException {
        super(source);

        inputOverlapDelaySamples = source.readInt();
        encoderDelaySamples = source.readInt();
    }

    public static RiffChunkReader<Atrac9FactChunk> reader() {
        return (source, _) -> new Atrac9FactChunk(source);
    }

    public int inputOverlapDelaySamples() {
        return inputOverlapDelaySamples;
    }

    public int encoderDelaySamples() {
        return encoderDelaySamples;
    }
}
