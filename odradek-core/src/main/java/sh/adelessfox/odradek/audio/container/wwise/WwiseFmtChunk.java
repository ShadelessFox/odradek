package sh.adelessfox.odradek.audio.container.wwise;

import sh.adelessfox.odradek.audio.container.riff.RiffChunkReader;
import sh.adelessfox.odradek.audio.container.wave.WaveFmtChunk;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.OptionalInt;

public final class WwiseFmtChunk extends WaveFmtChunk {
    public static final Id<WwiseFmtChunk> ID = new Id<>("fmt ", WwiseFmtChunk.class);

    private final OptionalInt sampleCount;

    private WwiseFmtChunk(BinaryReader reader) throws IOException {
        super(reader);

        var size = Short.toUnsignedInt(reader.readShort());
        var end = reader.position() + size;

        sampleCount = switch (formatTag()) {
            case 0xFFFF -> {
                // Vorbis
                if (size >= 6) {
                    reader.skip(6);
                } else if (size >= 2) {
                    reader.skip(2);
                }
                if (size == 24) {
                    reader.skip(16); // ubyte sig_unk[16];
                }
                yield OptionalInt.of(reader.readInt());
            }
            case 0xFFFE -> {
                // PCM
                yield OptionalInt.empty();
            }
            default -> throw new IOException("Unknown Wwise fmt chunk format tag: " + formatTag());
        };

        reader.position(end);
    }

    public static RiffChunkReader<WwiseFmtChunk> reader() {
        return (reader, _) -> new WwiseFmtChunk(reader);
    }

    public OptionalInt sampleCount() {
        return sampleCount;
    }
}
