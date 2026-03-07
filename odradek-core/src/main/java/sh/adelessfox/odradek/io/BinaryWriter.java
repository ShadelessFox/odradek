package sh.adelessfox.odradek.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;

public interface BinaryWriter extends Closeable {
    @FunctionalInterface
    interface Mapper<T> {
        void write(T value, BinaryWriter writer) throws IOException;
    }

    static BinaryWriter open(Path path) throws IOException {
        return ChannelBinaryWriter.open(path);
    }

    void writeByte(byte value) throws IOException;

    void writeShort(short value) throws IOException;

    void writeInt(int value) throws IOException;

    void writeLong(long value) throws IOException;

    default void writeFloat(float value) throws IOException {
        writeInt(Float.floatToIntBits(value));
    }

    default void writeDouble(double value) throws IOException {
        writeLong(Double.doubleToLongBits(value));
    }

    default void writeBytes(byte[] values) throws IOException {
        for (byte value : values) {
            writeByte(value);
        }
    }

    default void writeShorts(short[] values) throws IOException {
        for (short value : values) {
            writeShort(value);
        }
    }

    default void writeInts(int[] values) throws IOException {
        for (int value : values) {
            writeInt(value);
        }
    }

    default void writeLongs(long[] values) throws IOException {
        for (long value : values) {
            writeLong(value);
        }
    }

    default void writeFloats(float[] values) throws IOException {
        for (float value : values) {
            writeFloat(value);
        }
    }

    default void writeDoubles(double[] values) throws IOException {
        for (double value : values) {
            writeDouble(value);
        }
    }

    long position() throws IOException;

    BinaryWriter position(long pos) throws IOException;

    ByteOrder order();

    BinaryWriter order(ByteOrder order) throws IOException;
}
