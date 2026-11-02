package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.util.Check;

import java.io.IOException;

final class BytesBinaryReader extends BinaryReader {
    private final Bytes bytes;
    private int position = 0;

    BytesBinaryReader(Bytes bytes) {
        super(bytes.length());
        this.bytes = bytes;
    }

    @Override
    public void readBytes(Bytes.Mutable target) throws IOException {
        ensureRemaining(target.length());
        bytes.slice(position, target.length()).copyTo(target, 0);
        position += target.length();
    }

    @Override
    public byte readByte() throws IOException {
        ensureRemaining(Byte.BYTES);
        byte result = bytes.get(position);
        position += Byte.BYTES;
        return result;
    }

    @Override
    public short readShort() throws IOException {
        ensureRemaining(Short.BYTES);
        short result = bytes.getShort(position);
        position += Short.BYTES;
        return bigEndian ? Short.reverseBytes(result) : result;
    }

    @Override
    public int readInt() throws IOException {
        ensureRemaining(Integer.BYTES);
        int result = bytes.getInt(position);
        position += Integer.BYTES;
        return bigEndian ? Integer.reverseBytes(result) : result;
    }

    @Override
    public long readLong() throws IOException {
        ensureRemaining(Long.BYTES);
        long result = bytes.getLong(position);
        position += Long.BYTES;
        return bigEndian ? Long.reverseBytes(result) : result;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public BinaryReader position(long position) {
        this.position = Math.toIntExact(Check.position(position, size, "position"));
        return this;
    }

    @Override
    public BinaryReader slice(long offset, long length) {
        Check.fromIndexSize(offset, length, size);
        Bytes sliced = bytes.slice(Math.toIntExact(offset), Math.toIntExact(length));
        return new BytesBinaryReader(sliced);
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public String toString() {
        return "ByteArrayDataSource[position=" + position + ", size=" + size + "]";
    }
}
