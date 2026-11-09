package sh.adelessfox.odradek.audio.container.riff;

import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

@FunctionalInterface
public interface RiffChunkReader<T extends RiffChunk> {
    T read(BinarySource source, int size) throws IOException;
}
