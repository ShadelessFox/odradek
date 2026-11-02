package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.util.Check;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A generic source of data.
 * <p>
 * By default, underlying data is interpreted as little endian. Byte order can
 * be controlled using {@link #order(ByteOrder)} method.
 */
public abstract class BinaryReader implements Closeable {
    final long size;
    boolean bigEndian;

    BinaryReader(long size) {
        this.size = Check.positiveOrZero(size, "size");
        this.bigEndian = false;
    }

    public static BinaryReader wrap(ByteBuffer buffer) {
        return new BytesBinaryReader(Bytes.wrap(buffer));
    }

    public static BinaryReader wrap(byte[] array) {
        return new BytesBinaryReader(Bytes.wrap(array));
    }

    public static BinaryReader wrap(byte[] array, int off, int len) {
        return new BytesBinaryReader(Bytes.wrap(array, off, len));
    }

    public static BinaryReader open(Path path) throws IOException {
        return FileBinaryReader.create(path);
    }

    public abstract void readBytes(Bytes.Mutable target) throws IOException;

    public abstract byte readByte() throws IOException;

    public abstract short readShort() throws IOException;

    public abstract int readInt() throws IOException;

    public abstract long readLong() throws IOException;

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public float readHalf() throws IOException {
        return Float.float16ToFloat(readShort());
    }

    public byte[] readBytes(int count) throws IOException {
        var dst = new byte[count];
        readBytes(Bytes.Mutable.wrap(dst));
        return dst;
    }

    public short[] readShorts(int count) throws IOException {
        var dst = new short[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readShort();
        }
        return dst;
    }

    public int[] readInts(int count) throws IOException {
        var dst = new int[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readInt();
        }
        return dst;
    }

    public long[] readLongs(int count) throws IOException {
        var dst = new long[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readLong();
        }
        return dst;
    }

    public float[] readFloats(int count) throws IOException {
        var dst = new float[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readFloat();
        }
        return dst;
    }

    public double[] readDoubles(int count) throws IOException {
        var dst = new double[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readDouble();
        }
        return dst;
    }

    public float[] readHalfs(int count) throws IOException {
        var dst = new float[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readHalf();
        }
        return dst;
    }

    public String readString(StringFormat format) throws IOException {
        return readString(format, StandardCharsets.UTF_8);
    }

    public String readString(StringFormat format, Charset charset) throws IOException {
        int length = switch (format) {
            case BYTE_LENGTH -> Byte.toUnsignedInt(readByte());
            case SHORT_LENGTH -> Short.toUnsignedInt(readShort());
            case INT_LENGTH -> readInt();
        };
        return readString(length, charset);
    }

    public String readString(int length) throws IOException {
        return readString(length, StandardCharsets.UTF_8);
    }

    public String readString(int length, Charset charset) throws IOException {
        if (length == 0) {
            return "";
        }
        return new String(readBytes(length), charset);
    }

    public boolean readBool(BoolFormat format) throws IOException {
        int value = switch (format) {
            case BYTE -> readByte();
            case SHORT -> readShort();
            case INT -> readInt();
        };
        return switch (value) {
            case 0 -> false;
            case 1 -> true;
            default -> throw new IOException("Unexpected value for bool: " + value);
        };
    }

    public <T> List<T> readObjects(int count, Mapper<T> mapper) throws IOException {
        var dst = new ArrayList<T>(count);
        for (int i = 0; i < count; i++) {
            dst.add(mapper.read(this));
        }
        return List.copyOf(dst);
    }

    public void expectInt(int expected) throws IOException {
        int actual = readInt();
        if (actual != expected) {
            throw new IOException("Expected int " + expected + ", but got " + actual);
        }
    }

    public final long size() {
        return size;
    }

    public abstract long position();

    public abstract BinaryReader position(long pos);

    public abstract BinaryReader slice(long offset, long length);

    public final ByteOrder order() {
        return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    public final BinaryReader order(ByteOrder order) {
        this.bigEndian = Check.nonNull(order, "order") == ByteOrder.BIG_ENDIAN;
        return this;
    }

    public final long remaining() {
        return size - position();
    }

    public final BinaryReader skip(int count) throws IOException {
        Objects.checkIndex(count, Integer.MAX_VALUE);
        return position(position() + count);
    }

    /**
     * Throws if fewer than {@code expected} bytes remain.
     *
     * @param expected minimum number of bytes that must remain
     * @throws EOFException if fewer than {@code expected} bytes remain
     * @throws IOException  if the position cannot be determined
     */
    public final void ensureRemaining(long expected) throws IOException {
        Check.positiveOrZero(expected, "expected");
        if (remaining() < expected) {
            throw eof(expected);
        }
    }

    final EOFException eof(long expected) {
        return new EOFException("Expected at least " + expected + " bytes remaining, but only " + remaining() + " are available");
    }

    @FunctionalInterface
    public interface Mapper<T> {
        T read(BinaryReader reader) throws IOException;
    }
}
