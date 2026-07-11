package sh.adelessfox.odradek.io;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

final class ChannelBinaryWriter implements BinaryWriter {
    private final ByteBuffer buffer = ByteBuffer.allocate(16384).order(ByteOrder.LITTLE_ENDIAN);
    private final FileChannel channel;
    private long position;

    private ChannelBinaryWriter(FileChannel channel) {
        this.channel = channel;
    }

    static ChannelBinaryWriter open(Path path) throws IOException {
        return new ChannelBinaryWriter(FileChannel.open(path, WRITE, CREATE, TRUNCATE_EXISTING));
    }

    @Override
    public void write(ByteBuffer src) throws IOException {
        int remaining = src.remaining();
        if (remaining > buffer.capacity()) {
            flush();
            writeInternal(src);
            position += remaining;
            return;
        }
        if (remaining > buffer.remaining()) {
            flush();
        }
        buffer.put(src);
    }

    @Override
    public void writeByte(byte value) throws IOException {
        reserve(Byte.BYTES);
        buffer.put(value);
    }

    @Override
    public void writeShort(short value) throws IOException {
        reserve(Short.BYTES);
        buffer.putShort(value);
    }

    @Override
    public void writeInt(int value) throws IOException {
        reserve(Integer.BYTES);
        buffer.putInt(value);
    }

    @Override
    public void writeLong(long value) throws IOException {
        reserve(Long.BYTES);
        buffer.putLong(value);
    }

    @Override
    public long position() {
        return position + buffer.position();
    }

    @Override
    public BinaryWriter position(long pos) throws IOException {
        if (position() != pos) {
            flush();
            channel.position(pos);
            position = pos;
        }
        return this;
    }

    @Override
    public ByteOrder order() {
        return buffer.order();
    }

    @Override
    public BinaryWriter order(ByteOrder order) {
        buffer.order(order);
        return this;
    }

    @Override
    public void close() throws IOException {
        flush();
        channel.close();
    }

    private void reserve(int count) throws IOException {
        if (buffer.capacity() < count) {
            throw new IllegalArgumentException("Can't reserve more bytes than the buffer can hold");
        }
        if (buffer.remaining() < count) {
            flush();
        }
    }

    private void flush() throws IOException {
        int count = buffer.position();
        if (count > 0) {
            buffer.flip();
            writeInternal(buffer);
            buffer.clear();
            position += count;
        }
    }

    private void writeInternal(ByteBuffer src) throws IOException {
        while (src.hasRemaining()) {
            if (channel.write(src) < 0) {
                throw new EOFException();
            }
        }
    }
}
