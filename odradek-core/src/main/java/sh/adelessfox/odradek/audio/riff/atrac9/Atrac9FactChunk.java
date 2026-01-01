package sh.adelessfox.odradek.audio.riff.atrac9;

import sh.adelessfox.odradek.audio.riff.RiffChunkReader;
import sh.adelessfox.odradek.audio.riff.wave.WaveFactChunk;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public final class Atrac9FactChunk extends WaveFactChunk {
    public static final Id<Atrac9FactChunk> ID = new Id<>("fact", Atrac9FactChunk.class);

    private final int inputOverlapDelaySamples;
    private final int encoderDelaySamples;

    public Atrac9FactChunk(BinaryReader reader) throws IOException {
        super(reader);

        inputOverlapDelaySamples = reader.readInt();
        encoderDelaySamples = reader.readInt();
    }

    public static RiffChunkReader<Atrac9FactChunk> reader() {
        return (reader, _) -> new Atrac9FactChunk(reader);
    }

    public int inputOverlapDelaySamples() {
        return inputOverlapDelaySamples;
    }

    public int encoderDelaySamples() {
        return encoderDelaySamples;
    }
}
