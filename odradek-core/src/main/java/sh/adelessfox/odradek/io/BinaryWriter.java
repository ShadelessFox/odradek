package sh.adelessfox.odradek.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public interface BinaryWriter extends Closeable {
    @FunctionalInterface
    interface Mapper<T> {
        void write(T value, BinaryWriter writer) throws IOException;
    }

    static BinaryWriter open(Path path) throws IOException {
        return ChannelBinaryWriter.open(path);
    }

    void write(ByteBuffer src) throws IOException;

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
        write(ByteBuffer.wrap(values));
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

    default void writeBool(boolean value, BoolFormat format) throws IOException {
        switch (format) {
            case BYTE -> writeByte((byte) (value ? 1 : 0));
            case SHORT -> writeShort((short) (value ? 1 : 0));
            case INT -> writeInt(value ? 1 : 0);
        }
    }

    default void writeString(String value, StringFormat format) throws IOException {
        writeString(value, format, StandardCharsets.UTF_8);
    }

    default void writeString(String value, StringFormat format, Charset charset) throws IOException {
        var data = value.getBytes(charset);
        switch (format) {
            case BYTE_LENGTH -> {
                if (data.length > 255) {
                    throw new IllegalArgumentException(
                        "String too long for BYTE_LENGTH format: " + data.length + " bytes");
                }
                writeByte((byte) data.length);
            }
            case SHORT_LENGTH -> {
                if (data.length > 65535) {
                    throw new IllegalArgumentException(
                        "String too long for SHORT_LENGTH format: " + data.length + " bytes");
                }
                writeShort((short) data.length);
            }
            case INT_LENGTH -> writeInt((int) data.length);
        }
        writeBytes(data);
    }

    long position() throws IOException;

    BinaryWriter position(long pos) throws IOException;

    ByteOrder order();

    BinaryWriter order(ByteOrder order) throws IOException;
}
