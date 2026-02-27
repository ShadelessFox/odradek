package sh.adelessfox.odradek.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public final class ByteArrayBinaryWriter implements BinaryWriter {
    private ByteBuffer buffer = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);
    private int length = 0;

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
        return buffer.position();
    }

    @Override
    public BinaryWriter position(long pos) throws IOException {
        Objects.checkIndex(pos, Integer.MAX_VALUE);
        int curPos = buffer.position();
        int newPos = Math.toIntExact(Math.max(curPos, pos));
        if (curPos != newPos) {
            reserve(newPos - curPos);
            buffer.position(newPos);
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
    public void close() {
        buffer = null;
    }

    public byte[] toByteArray() {
        byte[] dst = new byte[length];
        buffer.get(0, dst, 0, length);
        return dst;
    }

    private void reserve(int count) {
        if (buffer.remaining() < count) {
            buffer = ByteBuffer
                .allocate(Math.max(buffer.capacity() * 2, buffer.capacity() + count))
                .order(buffer.order())
                .put(buffer.flip());
        }
        length = Math.max(length, buffer.position() + count);
    }
}
