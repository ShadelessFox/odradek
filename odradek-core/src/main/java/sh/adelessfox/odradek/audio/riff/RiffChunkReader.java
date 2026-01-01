package sh.adelessfox.odradek.audio.riff;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

@FunctionalInterface
public interface RiffChunkReader<T extends RiffChunk> {
    T read(BinaryReader reader, int size) throws IOException;
}
