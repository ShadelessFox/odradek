package sh.adelessfox.odradek.audio.container.wave;

import sh.adelessfox.odradek.audio.container.riff.RiffChunk;
import sh.adelessfox.odradek.audio.container.riff.RiffChunkReader;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class WaveDataChunk implements RiffChunk {
    public static final Id<WaveDataChunk> ID = new Id<>("data", WaveDataChunk.class);

    private final Bytes data;

    public WaveDataChunk(BinarySource source, int size) throws IOException {
        data = source.readBytes(size);
    }

    public static RiffChunkReader<? extends WaveDataChunk> reader() {
        return WaveDataChunk::new;
    }

    public Bytes data() {
        return data;
    }
}
