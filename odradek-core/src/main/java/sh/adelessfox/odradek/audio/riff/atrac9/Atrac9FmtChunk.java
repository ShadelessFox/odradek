package sh.adelessfox.odradek.audio.riff.atrac9;

import sh.adelessfox.odradek.audio.riff.RiffChunkReader;
import sh.adelessfox.odradek.audio.riff.wave.WaveFmtChunk;
import sh.adelessfox.odradek.audio.riff.wave.WaveFormatExtensible;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public final class Atrac9FmtChunk extends WaveFmtChunk {
    public static final Id<Atrac9FmtChunk> ID = new Id<>("fmt ", Atrac9FmtChunk.class);

    public Atrac9FmtChunk(BinaryReader reader) throws IOException {
        super(reader);
    }

    public static RiffChunkReader<Atrac9FmtChunk> reader() {
        return (reader, _) -> new Atrac9FmtChunk(reader);
    }

    @Override
    public Atrac9FormatExtensible extension() {
        return (Atrac9FormatExtensible) super.extension();
    }

    @Override
    protected WaveFormatExtensible readExtension(BinaryReader reader) throws IOException {
        return new Atrac9FormatExtensible(reader);
    }
}
