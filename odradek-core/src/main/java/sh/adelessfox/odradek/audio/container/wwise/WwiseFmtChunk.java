package sh.adelessfox.odradek.audio.container.wwise;

import sh.adelessfox.odradek.audio.container.riff.RiffChunkReader;
import sh.adelessfox.odradek.audio.container.wave.WaveFmtChunk;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public final class WwiseFmtChunk extends WaveFmtChunk {
    public static final Id<WwiseFmtChunk> ID = new Id<>("fmt ", WwiseFmtChunk.class);

    private final int sampleCount;

    private WwiseFmtChunk(BinaryReader reader) throws IOException {
        super(reader);

        int size = Short.toUnsignedInt(reader.readShort());
        var end = reader.position() + size;
        if (size >= 2) {
            reader.skip(2); // ushort ext_unk;
        }
        if (size >= 6) {
            reader.skip(4); // uint subtype;
        }
        if (size == 24) {
            reader.skip(16); // ubyte sig_unk[16];
        }

        sampleCount = reader.readInt();
        reader.position(end);
    }

    public static RiffChunkReader<WwiseFmtChunk> reader() {
        return (reader, _) -> new WwiseFmtChunk(reader);
    }

    public int sampleCount() {
        return sampleCount;
    }
}
