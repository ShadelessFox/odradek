package sh.adelessfox.odradek.audio.container.wave;

import sh.adelessfox.odradek.audio.container.riff.RiffChunk;
import sh.adelessfox.odradek.audio.container.riff.RiffChunkReader;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class WaveDataChunk implements RiffChunk {
    public static final Id<WaveDataChunk> ID = new Id<>("data", WaveDataChunk.class);

    private final byte[] data;

    public WaveDataChunk(BinaryReader reader, int size) throws IOException {
        data = reader.readBytes(size);
    }

    public static RiffChunkReader<? extends WaveDataChunk> reader() {
        return WaveDataChunk::new;
    }

    public byte[] data() {
        return data;
    }
}
