package sh.adelessfox.odradek.io;

import java.io.Closeable;
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
public interface BinaryReader extends Closeable {
    @FunctionalInterface
    interface Mapper<T> {
        T read(BinaryReader reader) throws IOException;
    }

    static BinaryReader wrap(ByteBuffer buffer) {
        if (!buffer.hasArray()) {
            throw new IllegalArgumentException("Buffer must be backed by an array");
        }
        return new BytesBinaryReader(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
    }

    static BinaryReader wrap(byte[] array) {
        return new BytesBinaryReader(array, 0, array.length);
    }

    static BinaryReader wrap(byte[] array, int off, int len) {
        return new BytesBinaryReader(array, off, len);
    }

    static BinaryReader open(Path path) throws IOException {
        return ChannelBinaryReader.open(path);
    }

    void readBytes(byte[] dst, int off, int len) throws IOException;

    byte readByte() throws IOException;

    short readShort() throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    default float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    default double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    default float readHalf() throws IOException {
        return Float.float16ToFloat(readShort());
    }

    default byte[] readBytes(int count) throws IOException {
        var dst = new byte[count];
        readBytes(dst, 0, count);
        return dst;
    }

    default short[] readShorts(int count) throws IOException {
        var dst = new short[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readShort();
        }
        return dst;
    }

    default int[] readInts(int count) throws IOException {
        var dst = new int[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readInt();
        }
        return dst;
    }

    default long[] readLongs(int count) throws IOException {
        var dst = new long[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readLong();
        }
        return dst;
    }

    default float[] readFloats(int count) throws IOException {
        var dst = new float[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readFloat();
        }
        return dst;
    }

    default double[] readDoubles(int count) throws IOException {
        var dst = new double[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readDouble();
        }
        return dst;
    }

    default float[] readHalfs(int count) throws IOException {
        var dst = new float[count];
        for (int i = 0; i < count; i++) {
            dst[i] = readHalf();
        }
        return dst;
    }

    default String readString(StringFormat format) throws IOException {
        return readString(format, StandardCharsets.UTF_8);
    }

    default String readString(StringFormat format, Charset charset) throws IOException {
        int length = switch (format) {
            case BYTE_LENGTH -> Byte.toUnsignedInt(readByte());
            case SHORT_LENGTH -> Short.toUnsignedInt(readShort());
            case INT_LENGTH -> readInt();
        };
        return readString(length, charset);
    }

    default String readString(int length) throws IOException {
        return readString(length, StandardCharsets.UTF_8);
    }

    default String readString(int length, Charset charset) throws IOException {
        if (length == 0) {
            return "";
        }
        return new String(readBytes(length), charset);
    }

    default boolean readBool(BoolFormat format) throws IOException {
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

    default <T> List<T> readObjects(int count, Mapper<T> mapper) throws IOException {
        var dst = new ArrayList<T>(count);
        for (int i = 0; i < count; i++) {
            dst.add(mapper.read(this));
        }
        return List.copyOf(dst);
    }

    long size() throws IOException;

    long position() throws IOException;

    BinaryReader position(long pos) throws IOException;

    ByteOrder order();

    BinaryReader order(ByteOrder order);

    default long remaining() throws IOException {
        return size() - position();
    }

    default void skip(int count) throws IOException {
        Objects.checkIndex(count, Integer.MAX_VALUE);
        position(position() + count);
    }
}
