package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.Bytes;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class FileBinaryReader extends BufferedBinaryReader {
    private final FileChannel channel;

    private FileBinaryReader(FileChannel channel) throws IOException {
        super(channel.size());
        this.channel = channel;
    }

    static FileBinaryReader create(Path path) throws IOException {
        var channel = FileChannel.open(path, StandardOpenOption.READ);
        trySetUninterruptible(channel);
        return new FileBinaryReader(channel);
    }

    @Override
    int readImpl(Bytes.Mutable target, long position) throws IOException {
        return Math.max(channel.read(target.asMutableBuffer(), position), 0);
    }

    @Override
    public void close() throws IOException {
        reset();
        channel.close();
    }

    @SuppressWarnings("Java9ReflectionClassVisibility")
    private static void trySetUninterruptible(FileChannel channel) {
        try {
            var fileChannelImpl = Class.forName("sun.nio.ch.FileChannelImpl");
            var setUninterruptible = fileChannelImpl.getDeclaredMethod("setUninterruptible");
            if (!setUninterruptible.canAccess(channel)) {
                setUninterruptible.setAccessible(true);
            }
            setUninterruptible.invoke(channel);
        } catch (Exception ignored) {
        }
    }
}
