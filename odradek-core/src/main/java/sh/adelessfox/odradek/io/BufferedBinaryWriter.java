package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.util.Check;

import java.io.EOFException;
import java.io.IOException;

abstract class BufferedBinaryWriter extends BinaryWriter {
    private static final int BUFFER_SIZE = 0x2000;

    private final Bytes.Mutable buffer = Bytes.allocate(BUFFER_SIZE);
    private long sourcePosition = 0; // Always points to buffer[0]
    private int bufferPosition = 0; // Points to the next byte to write
    private int bufferLength = 0; // Number of bytes in the buffer

    /**
     * Writes up to {@code source.length()} bytes at the given absolute position.
     *
     * @param source   the source of bytes to write
     * @param position the absolute position to write at
     * @return the number of bytes actually written, or {@code -1} if the end of the output is reached
     */
    abstract int writeImpl(Bytes source, long position) throws IOException;

    @Override
    public final long position() {
        return sourcePosition + bufferPosition;
    }

    @Override
    public final BinaryWriter position(long pos) throws IOException {
        Check.positiveOrZero(pos, "position");
        if (pos >= sourcePosition && pos - sourcePosition <= bufferLength) {
            // If we fit in the current buffer, just adjust the position
            bufferPosition = (int) (pos - sourcePosition);
        } else {
            // If not, flush the buffer and mark it empty
            flush();
            sourcePosition = pos;
        }
        return this;
    }

    @Override
    public final void writeBytes(Bytes values) throws IOException {
        if (values.length() == 0) {
            return;
        }

        // If the buffer has enough space, just copy the data and return
        if (bufferRemaining() >= values.length()) {
            values.copyTo(buffer, bufferPosition);
            bufferPosition += values.length();
            bufferLength = Math.max(bufferLength, bufferPosition);
            return;
        }

        // Otherwise, write directly to the output
        flush();
        writeInternal(values, sourcePosition);
        sourcePosition += values.length();
    }

    @Override
    public final void writeByte(byte value) throws IOException {
        reserve(Byte.BYTES);
        buffer.set(bufferPosition, value);
        bufferPosition++;
    }

    @Override
    public final void writeShort(short value) throws IOException {
        reserve(Short.BYTES);
        buffer.setShort(bufferPosition, bigEndian ? Short.reverseBytes(value) : value);
        bufferPosition += Short.BYTES;
    }

    @Override
    public final void writeInt(int value) throws IOException {
        reserve(Integer.BYTES);
        buffer.setInt(bufferPosition, bigEndian ? Integer.reverseBytes(value) : value);
        bufferPosition += Integer.BYTES;
    }

    @Override
    public final void writeLong(long value) throws IOException {
        reserve(Long.BYTES);
        buffer.setLong(bufferPosition, bigEndian ? Long.reverseBytes(value) : value);
        bufferPosition += Long.BYTES;
    }

    private void reserve(int count) throws IOException {
        if (count > bufferRemaining()) {
            flush();
        }
        bufferLength = Math.max(bufferLength, bufferPosition + count);
    }

    @Override
    public final void flush() throws IOException {
        writeInternal(buffer.slice(0, bufferLength), sourcePosition);

        sourcePosition += bufferPosition;
        bufferPosition = 0;
        bufferLength = 0;
    }

    private void writeInternal(Bytes source, long position) throws IOException {
        int written = 0;
        while (written < source.length()) {
            int count = writeImpl(source.slice(written), position + written);
            if (count < 0) {
                throw new EOFException("Unexpected end of output");
            }
            if (count == 0 || count > source.length() - written) {
                throw new IOException("Invalid write progress: " + count);
            }
            written += count;
        }
    }

    private int bufferRemaining() {
        return BUFFER_SIZE - bufferPosition;
    }

    void reset() {
        sourcePosition = 0;
        bufferPosition = 0;
        bufferLength = 0;
    }
}
