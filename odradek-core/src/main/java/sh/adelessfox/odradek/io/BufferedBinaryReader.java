package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.util.Check;

import java.io.IOException;

abstract class BufferedBinaryReader extends BinaryReader {
    private static final int BUFFER_SIZE = 0x2000;

    private final Bytes.Mutable buffer = Bytes.allocate(BUFFER_SIZE);
    private long sourcePosition = 0; // Always points to buffer[0]
    private int bufferPosition = 0; // Points to the next byte to read
    private int bufferLength = 0; // Number of bytes in the buffer

    BufferedBinaryReader(long size) {
        super(size);
    }

    abstract int readImpl(Bytes.Mutable target, long position) throws IOException;

    @Override
    public final long position() {
        return sourcePosition + bufferPosition;
    }

    @Override
    public final BinaryReader position(long position) {
        Check.position(position, size, "position");

        if (position >= sourcePosition && position < sourcePosition + bufferLength) {
            // If we fit in the current buffer, just adjust the position
            bufferPosition = (int) (position - sourcePosition);
        } else {
            // If not, move the channel position and mark the buffer empty
            sourcePosition = position;
            bufferPosition = 0;
            bufferLength = 0;
        }
        return this;
    }

    @Override
    public final BinaryReader slice(long offset, long length) {
        Check.fromIndexSize(offset, length, this.size);
        return new SliceBinaryReader(this, offset, length);
    }

    @Override
    public final void readBytes(Bytes.Mutable target) throws IOException {
        ensureRemaining(target.length());

        // If the buffer has enough data, just copy the data and return
        if (bufferRemaining() >= target.length()) {
            buffer.slice(bufferPosition, target.length()).copyTo(target, 0);
            bufferPosition += target.length();
            return;
        }

        // If there's remaining data in the buffer, copy it first
        int targetPosition = 0;
        if (bufferPosition < bufferLength) {
            int remaining = bufferRemaining();
            buffer.slice(bufferPosition, remaining).copyTo(target, targetPosition);
            targetPosition += remaining;
        }

        // Advance even when the buffer was already exhausted before this read.
        sourcePosition += bufferLength;
        bufferPosition = 0;
        bufferLength = 0;

        // If the data we want to read fits in a single buffer, do a refill and copy
        int targetRemaining = target.length() - targetPosition;
        if (targetRemaining < BUFFER_SIZE) {
            refill(targetRemaining); // Make sure we have enough data in the buffer
            buffer.slice(bufferPosition, targetRemaining).copyTo(target, targetPosition);
            bufferPosition += targetRemaining;
            return;
        }

        // If not, do a straight read, buffer is emptied
        while (targetPosition < target.length()) {
            int read = readImpl(target.slice(targetPosition, target.length() - targetPosition), sourcePosition);
            if (read <= 0) {
                throw eof(target.length() - targetPosition);
            }
            sourcePosition += read;
            targetPosition += read;
        }
    }

    @Override
    public final byte readByte() throws IOException {
        refill(Byte.BYTES);
        byte result = buffer.get(bufferPosition);
        bufferPosition++;
        return result;
    }

    @Override
    public final short readShort() throws IOException {
        refill(Short.BYTES);
        short result = buffer.getShort(bufferPosition);
        bufferPosition += Short.BYTES;
        return bigEndian ? Short.reverseBytes(result) : result;
    }

    @Override
    public final int readInt() throws IOException {
        refill(Integer.BYTES);
        int result = buffer.getInt(bufferPosition);
        bufferPosition += Integer.BYTES;
        return bigEndian ? Integer.reverseBytes(result) : result;
    }

    @Override
    public final long readLong() throws IOException {
        refill(Long.BYTES);
        long result = buffer.getLong(bufferPosition);
        bufferPosition += Long.BYTES;
        return bigEndian ? Long.reverseBytes(result) : result;
    }

    private void refill(int length) throws IOException {
        ensureRemaining(length);
        int remaining = bufferRemaining();
        if (remaining >= length) {
            return;
        }

        // First we have to move the leftover data to the front
        buffer.slice(bufferPosition, remaining).copyTo(buffer, 0);
        sourcePosition += bufferPosition;
        bufferPosition = 0;
        bufferLength = remaining;

        // Then we can copy in new data from the channel
        while (bufferLength < length) {
            int available = (int) Math.min(BUFFER_SIZE - bufferLength, size - sourcePosition - bufferLength);
            Bytes.Mutable target = buffer.slice(bufferLength, available);
            int read = readImpl(target, sourcePosition + bufferLength);
            if (read <= 0) {
                throw eof(length);
            }
            bufferLength += read;
        }
    }

    private int bufferRemaining() {
        return bufferLength - bufferPosition;
    }

    void reset() {
        sourcePosition = 0;
        bufferPosition = 0;
        bufferLength = 0;
    }

    private static final class SliceBinaryReader extends BufferedBinaryReader {
        private final BufferedBinaryReader source;
        private final long offset;

        SliceBinaryReader(BufferedBinaryReader source, long offset, long length) {
            super(length);
            this.source = source;
            this.offset = offset;
        }

        @Override
        int readImpl(Bytes.Mutable target, long position) throws IOException {
            int length = (int) Math.min(target.length(), size - position);
            if (length == 0) {
                return 0;
            }
            return source.readImpl(target.slice(0, length), offset + position);
        }

        @Override
        public void close() {
            // Do nothing
        }
    }
}
