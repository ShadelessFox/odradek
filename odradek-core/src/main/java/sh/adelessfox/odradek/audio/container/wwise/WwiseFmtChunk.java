package sh.adelessfox.odradek.audio.container.wwise;

import sh.adelessfox.odradek.audio.container.riff.RiffChunkReader;
import sh.adelessfox.odradek.audio.container.wave.WaveFmtChunk;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.OptionalInt;

public final class WwiseFmtChunk extends WaveFmtChunk {
    public static final Id<WwiseFmtChunk> ID = new Id<>("fmt ", WwiseFmtChunk.class);

    private final OptionalInt sampleCount;

    private WwiseFmtChunk(BinarySource source) throws IOException {
        super(source);

        var size = Short.toUnsignedInt(source.readShort());
        var end = source.position() + size;

        sampleCount = switch (formatTag()) {
            case 0xFFFF -> {
                // Vorbis
                if (size >= 6) {
                    source.skip(6);
                } else if (size >= 2) {
                    source.skip(2);
                }
                if (size == 24) {
                    source.skip(16); // ubyte sig_unk[16];
                }
                yield OptionalInt.of(source.readInt());
            }
            case 0xFFFE -> {
                // PCM
                yield OptionalInt.empty();
            }
            default -> throw new IOException("Unknown Wwise fmt chunk format tag: " + formatTag());
        };

        source.position(end);
    }

    public static RiffChunkReader<WwiseFmtChunk> reader() {
        return (source, _) -> new WwiseFmtChunk(source);
    }

    public OptionalInt sampleCount() {
        return sampleCount;
    }
}
