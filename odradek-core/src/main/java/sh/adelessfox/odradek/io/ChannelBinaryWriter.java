package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.Bytes;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

final class ChannelBinaryWriter extends BufferedBinaryWriter {
    private final FileChannel channel;

    private ChannelBinaryWriter(FileChannel channel) {
        this.channel = channel;
    }

    static ChannelBinaryWriter create(Path path) throws IOException {
        return new ChannelBinaryWriter(FileChannel.open(path, WRITE, CREATE, TRUNCATE_EXISTING));
    }

    @Override
    int writeImpl(Bytes source, long position) throws IOException {
        return channel.write(source.asBuffer(), position);
    }

    @Override
    public void close() throws IOException {
        try (channel) {
            flush();
            reset();
        }
    }
}
