package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.io.BoolFormat;
import wtf.reversed.toolbox.io.StringFormat;
import wtf.reversed.toolbox.util.Check;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Random-access binary writer with a configurable byte order. Supports writing primitives,
 * bulk writes from typed buffers, strings, and arbitrary user objects via a {@link Mapper}.
 * <p>
 * Methods that write data, such as {@code writeInt}, start at the current {@link #position() position}
 * and advance it by the number of bytes written. Multibyte values are encoded using the current
 * {@link #order() byte order}, which defaults to {@link ByteOrder#LITTLE_ENDIAN LITTLE_ENDIAN}.
 */
public abstract class BinaryWriter implements Flushable, Closeable {
    boolean bigEndian;

    public static BinaryWriter open(Path path) throws IOException {
        return ChannelBinaryWriter.create(path);
    }

    /**
     * Returns the current position of the writer in bytes.
     *
     * @return current position
     */
    public abstract long position();

    /**
     * Sets the current position of the writer in bytes.
     *
     * @param pos new position
     * @return this writer
     * @throws IOException if the seek fails
     */
    public abstract BinaryWriter position(long pos) throws IOException;

    /**
     * Returns the byte order used when writing multibyte primitives.
     *
     * @return byte order
     */
    public final ByteOrder order() {
        return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * Sets the byte order used when writing multibyte primitives.
     *
     * @param order new byte order
     * @return this writer
     */
    public final BinaryWriter order(ByteOrder order) {
        this.bigEndian = Check.nonNull(order, "order") == ByteOrder.BIG_ENDIAN;
        return this;
    }

    /**
     * Writes a single signed byte.
     *
     * @param value the value to write
     * @throws IOException if the write fails
     */
    public abstract void writeByte(byte value) throws IOException;

    /**
     * Writes a 16-bit signed integer.
     *
     * @param value the value to write
     * @throws IOException if the write fails
     */
    public abstract void writeShort(short value) throws IOException;

    /**
     * Writes a 32-bit signed integer.
     *
     * @param value the value to write
     * @throws IOException if the write fails
     */
    public abstract void writeInt(int value) throws IOException;

    /**
     * Writes a 64-bit signed integer.
     *
     * @param value the value to write
     * @throws IOException if the write fails
     */
    public abstract void writeLong(long value) throws IOException;

    /**
     * Writes a 16-bit IEEE 754 half-precision float.
     *
     * @param value the value to write, converted to half-precision using {@link Float#floatToFloat16(float)}
     * @throws IOException if the write fails
     */
    public final void writeHalf(float value) throws IOException {
        writeShort(Float.floatToFloat16(value));
    }

    /**
     * Writes a 32-bit IEEE 754 single-precision float.
     *
     * @param value the value to write
     * @throws IOException if the write fails
     */
    public final void writeFloat(float value) throws IOException {
        writeInt(Float.floatToRawIntBits(value));
    }

    /**
     * Writes a 64-bit IEEE 754 double-precision float.
     *
     * @param value the value to write
     * @throws IOException if the write fails
     */
    public final void writeDouble(double value) throws IOException {
        writeLong(Double.doubleToRawLongBits(value));
    }

    /**
     * Writes a boolean encoded in the given format.
     *
     * @param format width of the underlying integer
     * @throws IOException if the write fails
     */
    public final void writeBool(boolean value, BoolFormat format) throws IOException {
        switch (format) {
            case BYTE -> writeByte((byte) (value ? 1 : 0));
            case SHORT -> writeShort((short) (value ? 1 : 0));
            case INT -> writeInt(value ? 1 : 0);
            default -> throw new AssertionError();
        }
    }

    /**
     * Writes a string in the given format. The string is encoded in UTF-8.
     *
     * @param value  the string to write
     * @param format the format to use
     * @throws IOException if the write fails
     */
    public void writeString(String value, StringFormat format) throws IOException {
        writeString(value, format, StandardCharsets.UTF_8);
    }

    /**
     * Writes a string in the given format. The string is encoded in the given charset.
     *
     * @param value   the string to write
     * @param format  the format to use
     * @param charset the charset to use
     * @throws IOException if the write fails
     */
    public void writeString(String value, StringFormat format, Charset charset) throws IOException {
        var bytes = Bytes.wrap(value.getBytes(charset));
        switch (format) {
            case BYTE_LENGTH -> {
                Check.argument(bytes.length() < 1L << 8, "string is too long for BYTE_LENGTH format");
                writeByte((byte) bytes.length());
                writeBytes(bytes);
            }
            case SHORT_LENGTH -> {
                Check.argument(bytes.length() < 1L << 16, "string is too long for SHORT_LENGTH format");
                writeShort((short) bytes.length());
                writeBytes(bytes);
            }
            case INT_LENGTH -> {
                writeInt(bytes.length());
                writeBytes(bytes);
            }
            case NULL_TERM -> throw new UnsupportedOperationException("NULL_TERM strings are not supported by BinaryWriter");
            default -> throw new AssertionError();
        }
    }

    /**
     * Writes bytes from {@code values}.
     *
     * @param values the values to write
     * @throws IOException if the write fails
     */
    public abstract void writeBytes(Bytes values) throws IOException;

    /**
     * Writes 16-bit signed integers from {@code values}.
     *
     * @param values the values to write
     * @throws IOException if the write fails
     */
    public final void writeShorts(Shorts values) throws IOException {
        if (values.length() == 0) {
            return;
        }
        if (order() == ByteOrder.LITTLE_ENDIAN) {
            writeBytes(values.asBytes());
        } else {
            for (int i = 0; i < values.length(); i++) {
                writeShort(values.get(i));
            }
        }
    }

    /**
     * Writes 32-bit signed integers from {@code values}.
     *
     * @param values the values to write
     * @throws IOException if the write fails
     */
    public final void writeInts(Ints values) throws IOException {
        if (values.length() == 0) {
            return;
        }
        if (order() == ByteOrder.LITTLE_ENDIAN) {
            writeBytes(values.asBytes());
        } else {
            for (int i = 0; i < values.length(); i++) {
                writeInt(values.get(i));
            }
        }
    }

    /**
     * Writes 64-bit signed integers from {@code values}.
     *
     * @param values the values to write
     * @throws IOException if the write fails
     */
    public final void writeLongs(Longs values) throws IOException {
        if (values.length() == 0) {
            return;
        }
        if (order() == ByteOrder.LITTLE_ENDIAN) {
            writeBytes(values.asBytes());
        } else {
            for (int i = 0; i < values.length(); i++) {
                writeLong(values.get(i));
            }
        }
    }

    /**
     * Writes 16-bit IEEE 754 half-precision floats from {@code values}.
     *
     * @param values the values to write
     * @throws IOException if the write fails
     */
    public final void writeHalfs(Floats values) throws IOException {
        for (int i = 0; i < values.length(); i++) {
            writeHalf(values.get(i));
        }
    }

    /**
     * Writes 32-bit IEEE 754 single-precision floats from {@code values}.
     *
     * @param values the values to write
     * @throws IOException if the write fails
     */
    public final void writeFloats(Floats values) throws IOException {
        if (values.length() == 0) {
            return;
        }
        if (order() == ByteOrder.LITTLE_ENDIAN) {
            writeBytes(values.asBytes());
        } else {
            for (int i = 0; i < values.length(); i++) {
                writeFloat(values.get(i));
            }
        }
    }

    /**
     * Writes 64-bit IEEE 754 double-precision floats from {@code values}.
     *
     * @param values the values to write
     * @throws IOException if the write fails
     */
    public final void writeDoubles(Doubles values) throws IOException {
        if (values.length() == 0) {
            return;
        }
        if (order() == ByteOrder.LITTLE_ENDIAN) {
            writeBytes(values.asBytes());
        } else {
            for (int i = 0; i < values.length(); i++) {
                writeDouble(values.get(i));
            }
        }
    }

    /**
     * Creates an {@link OutputStream} that writes to this {@link BinaryWriter}.
     * <p>
     * The stream will flush the writer when closed, but will not close the writer itself.
     *
     * @return an output stream that writes to this writer
     */
    public OutputStream asOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                writeByte((byte) value);
            }

            @Override
            public void write(byte[] bytes, int offset, int length) throws IOException {
                writeBytes(Bytes.wrap(bytes, offset, length));
            }

            @Override
            public void flush() throws IOException {
                BinaryWriter.this.flush();
            }

            @Override
            public void close() throws IOException {
                flush();
            }
        };
    }

    /**
     * Creates a {@link Writer} that writes to this {@link BinaryWriter}.
     * <p>
     * The writer will flush the binary writer when closed, but will not close the binary writer itself.
     *
     * @param charset the charset to use for encoding characters
     * @return a writer that writes to this binary writer
     */
    public Writer asWriter(Charset charset) {
        return new OutputStreamWriter(asOutputStream(), charset);
    }

    /**
     * Writes a single object to a {@link BinaryWriter}.
     *
     * @param <T> the type of the object to encode
     */
    @FunctionalInterface
    public interface Mapper<T> {
        /**
         * Writes a single object to a {@link BinaryWriter}.
         *
         * @param value  the object to write
         * @param writer the writer to write to
         * @throws IOException if the write fails
         */
        void write(T value, BinaryWriter writer) throws IOException;
    }
}
