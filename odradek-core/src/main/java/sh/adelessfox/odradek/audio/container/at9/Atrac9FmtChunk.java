package sh.adelessfox.odradek.audio.container.at9;

import sh.adelessfox.odradek.audio.container.riff.RiffChunkReader;
import sh.adelessfox.odradek.audio.container.wave.WaveFmtChunk;
import sh.adelessfox.odradek.audio.container.wave.WaveFormatExtensible;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public final class Atrac9FmtChunk extends WaveFmtChunk {
    public static final Id<Atrac9FmtChunk> ID = new Id<>("fmt ", Atrac9FmtChunk.class);

    public Atrac9FmtChunk(BinarySource source) throws IOException {
        super(source);
    }

    public static RiffChunkReader<Atrac9FmtChunk> reader() {
        return (source, _) -> new Atrac9FmtChunk(source);
    }

    @Override
    public Atrac9FormatExtensible extension() {
        return (Atrac9FormatExtensible) super.extension();
    }

    @Override
    protected WaveFormatExtensible readExtension(BinarySource source, int format) throws IOException {
        if (format == WAVE_FORMAT_EXTENSIBLE) {
            return new Atrac9FormatExtensible(source);
        }
        return null;
    }
}
