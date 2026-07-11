package sh.adelessfox.odradek.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.compress.Compressor;
import wtf.reversed.toolbox.util.Check;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * A writer for DirectStorage archives.
 *
 * <p>Supports only the LZ4 compression.
 *
 * @see <a href="https://github.com/ShadelessFox/decima/wiki/Archives#directstorage-archive">DirectStorage archive format</a>
 */
public final class DirectStorageWriter implements BinaryWriter {
    private static final int MAX_CHUNK_SIZE = 0x40000;

    private static final int HEADER_MAGIC = 'D' | 'S' << 8 | 'A' << 16 | 'R' << 24;
    private static final long HEADER_PADDING = 0x2a4b45444152444fL;
    private static final long CHUNK_PADDING = 0x5555555555555555L;
    private static final byte CHUNK_COMPRESSION_LZ4 = 3;
    private static final Logger log = LoggerFactory.getLogger(DirectStorageWriter.class);

    private final BinaryWriter writer;
    private final long dataSize;

    private final Compressor compressor = Compressor.lz4Block();
    private final ByteBuffer buffer = ByteBuffer.allocate(MAX_CHUNK_SIZE).order(ByteOrder.LITTLE_ENDIAN);
    private final Bytes.Mutable output = Bytes.allocate(compressor.maxCompressedLength(MAX_CHUNK_SIZE));
    private final List<Chunk> chunks;

    private long position;
    private long compressedPosition;

    /**
     * Creates a new writer.
     *
     * <p>The total uncompressed size must be supplied to reserve space for the header and chunk table.
     *
     * <p>If the data to be written will exceed the specified size,
     * an {@link IllegalStateException} will be thrown by {@code writeXXX}
     * methods of this class.
     *
     * @param writer              the writer to write to
     * @param maxDecompressedSize the total uncompressed size
     */
    public DirectStorageWriter(BinaryWriter writer, long maxDecompressedSize) {
        Check.positiveOrZero(maxDecompressedSize, "maxDecompressedSize");
        this.writer = writer;
        this.dataSize = maxDecompressedSize;
        this.chunks = new ArrayList<>(computeChunkCount(maxDecompressedSize));
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
    public long position() throws IOException {
        return position + buffer.position();
    }

    @Override
    public BinaryWriter position(long pos) throws IOException {
        throw new UnsupportedOperationException("positioning is not supported");
    }

    @Override
    public ByteOrder order() {
        return buffer.order();
    }

    @Override
    public BinaryWriter order(ByteOrder order) throws IOException {
        buffer.order(order);
        return this;
    }

    @Override
    public void close() throws IOException {
        flush();
        writeCompleteHeader();
        writer.close();

        if (position != dataSize) {
            log.warn("Expected to write {} bytes, but wrote {} bytes", dataSize, position);
        }
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
        if (position == 0) {
            // If this is the first write, reserve space for the header and chunk table
            int headerSize = computeHeaderSize(dataSize);
            writer.position(headerSize);
            compressedPosition = headerSize;
        }

        int size = src.remaining();
        int compressedSize = compressor.compress(Bytes.wrap(src), output);

        src.position(src.limit());
        writer.write(output.asMutableBuffer());

        chunks.add(new Chunk(position, compressedPosition, size, compressedSize));
        compressedPosition += compressedSize;
    }

    private void writeCompleteHeader() throws IOException {
        writer.position(0);
        writeHeader();
        for (Chunk chunk : chunks) {
            writeChunk(chunk);
        }
    }

    private void writeHeader() throws IOException {
        int chunkCount = computeChunkCount(dataSize);
        writer.writeInt(HEADER_MAGIC);
        writer.writeShort((short) 3);
        writer.writeShort((short) 1);
        writer.writeInt(chunkCount);
        writer.writeInt(computeHeaderSize(dataSize));
        writer.writeLong(dataSize);
        writer.writeLong(HEADER_PADDING);
    }

    private void writeChunk(Chunk chunk) throws IOException {
        writer.writeLong(chunk.offset());
        writer.writeLong(chunk.compressedOffset());
        writer.writeInt(chunk.size());
        writer.writeInt(chunk.compressedSize());
        writer.writeLong(CHUNK_PADDING & ~0xFF | CHUNK_COMPRESSION_LZ4);
    }

    private int computeHeaderSize(long dataSize) {
        return 32 + 32 * computeChunkCount(dataSize);
    }

    private static int computeChunkCount(long dataSize) {
        return Math.toIntExact(Math.ceilDiv(dataSize, MAX_CHUNK_SIZE));
    }

    private record Chunk(
        long offset,
        long compressedOffset,
        int size,
        int compressedSize
    ) {
    }
}
