package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.util.Check;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A source for compressed data split into chunks.
 */
public abstract class ChunkedBinarySource<T extends ChunkedBinarySource.Chunk> extends BinarySource {
    public interface Chunk {
        long offset();

        int size();

        long compressedOffset();

        int compressedSize();
    }

    private final BinarySource source;
    private final NavigableMap<Long, T> chunks;
    private final long offset;

    private final Bytes.Mutable compressed;
    private final Bytes.Mutable decompressed;
    private final Bytes.Mutable scratch = Bytes.allocate(8);

    private T chunk;
    private long position;

    private ChunkedBinarySource(
        BinarySource source,
        NavigableMap<Long, T> chunks,
        int maxCompressedSize,
        int maxDecompressedSize,
        long size,
        long offset
    ) {
        super(size);

        this.source = source;
        this.chunks = chunks;
        this.compressed = Bytes.Mutable.allocate(maxCompressedSize);
        this.decompressed = Bytes.Mutable.allocate(maxDecompressedSize);
        this.offset = offset;
    }

    public ChunkedBinarySource(BinarySource source, Collection<? extends T> chunks) {
        int maxCompressedSize = 0;
        int maxDecompressedSize = 0;
        var map = new TreeMap<Long, T>(Long::compareUnsigned);
        for (T chunk : chunks) {
            map.put(chunk.offset(), chunk);
            maxCompressedSize = Math.max(maxCompressedSize, chunk.compressedSize());
            maxDecompressedSize = Math.max(maxDecompressedSize, chunk.size());
        }

        var size = chunks.stream().mapToLong(Chunk::size).sum();
        this(source, map, maxCompressedSize, maxDecompressedSize, size, 0);
    }

    @Override
    public byte readByte() throws IOException {
        readBytes(scratch.slice(0, Byte.BYTES));
        return scratch.get(0);
    }

    @Override
    public short readShort() throws IOException {
        readBytes(scratch.slice(0, Short.BYTES));
        var result = scratch.getShort(0);
        return order() == ByteOrder.BIG_ENDIAN ? Short.reverseBytes(result) : result;
    }

    @Override
    public int readInt() throws IOException {
        readBytes(scratch.slice(0, Integer.BYTES));
        var result = scratch.getInt(0);
        return order() == ByteOrder.BIG_ENDIAN ? Integer.reverseBytes(result) : result;
    }

    @Override
    public long readLong() throws IOException {
        readBytes(scratch.slice(0, Long.BYTES));
        var result = scratch.getLong(0);
        return order() == ByteOrder.BIG_ENDIAN ? Long.reverseBytes(result) : result;
    }

    @Override
    public void readBytes(Bytes.Mutable target) throws IOException {
        ensureRemaining(target.length());
        int off = 0;
        int len = target.length();

        while (len > 0) {
            long absolutePosition = offset + position;
            T chunk = chunks.floorEntry(absolutePosition).getValue();
            int offset = Math.toIntExact(absolutePosition - chunk.offset());
            int length = Math.min(chunk.size() - offset, len);

            if (length == 0) {
                throw new EOFException();
            }

            if (this.chunk != chunk) {
                this.chunk = chunk;

                var compressedSlice = compressed.slice(0, chunk.compressedSize());
                var decompressedSlice = decompressed.slice(0, chunk.size());

                source.position(chunk.compressedOffset());
                source.readBytes(compressedSlice);
                decompress(compressedSlice, decompressedSlice, chunk);
            }

            decompressed.slice(offset, length).copyTo(target, off);
            position += length;
            off += length;
            len -= length;
        }
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public BinarySource position(long pos) {
        position = Check.position(pos, size(), "position");
        return this;
    }

    @Override
    public BinarySource slice(long offset, long length) {
        Check.fromIndexSize(offset, length, size());
        return new SliceBinarySource<>(
            source,
            chunks,
            compressed.length(),
            decompressed.length(),
            length,
            this.offset + offset,
            this);
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    protected abstract void decompress(Bytes.Mutable src, Bytes.Mutable dst, T chunk);

    private static final class SliceBinarySource<T extends Chunk> extends ChunkedBinarySource<T> {
        final ChunkedBinarySource<T> delegate;

        SliceBinarySource(
            BinarySource source,
            NavigableMap<Long, T> chunks,
            int maxCompressedSize,
            int maxDecompressedSize,
            long size,
            long baseOffset,
            ChunkedBinarySource<T> delegate
        ) {
            super(source, chunks, maxCompressedSize, maxDecompressedSize, size, baseOffset);
            this.delegate = delegate;
            order(delegate.order());
        }

        @Override
        protected void decompress(Bytes.Mutable src, Bytes.Mutable dst, T chunk) {
            delegate.decompress(src, dst, chunk);
        }

        @Override
        public void close() {
            // Do nothing
        }
    }
}
