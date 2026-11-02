package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.util.Check;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A reader for compressed data split into chunks.
 */
public abstract class ChunkedBinaryReader<T extends ChunkedBinaryReader.Chunk> extends BinaryReader {
    public interface Chunk {
        long offset();

        int size();

        long compressedOffset();

        int compressedSize();
    }

    private final BinaryReader reader;
    private final NavigableMap<Long, T> chunks;
    private final long offset;

    private final Bytes.Mutable compressed;
    private final Bytes.Mutable decompressed;
    private final Bytes.Mutable scratch = Bytes.allocate(8);

    private T chunk;
    private long position;

    private ChunkedBinaryReader(
        BinaryReader reader,
        NavigableMap<Long, T> chunks,
        int maxCompressedSize,
        int maxDecompressedSize,
        long size,
        long offset
    ) {
        super(size);

        this.reader = reader;
        this.chunks = chunks;
        this.compressed = Bytes.Mutable.allocate(maxCompressedSize);
        this.decompressed = Bytes.Mutable.allocate(maxDecompressedSize);
        this.offset = offset;
    }

    public ChunkedBinaryReader(BinaryReader reader, Collection<? extends T> chunks) {
        int maxCompressedSize = 0;
        int maxDecompressedSize = 0;
        var map = new TreeMap<Long, T>(Long::compareUnsigned);
        for (T chunk : chunks) {
            map.put(chunk.offset(), chunk);
            maxCompressedSize = Math.max(maxCompressedSize, chunk.compressedSize());
            maxDecompressedSize = Math.max(maxDecompressedSize, chunk.size());
        }

        var size = chunks.stream().mapToLong(Chunk::size).sum();
        this(reader, map, maxCompressedSize, maxDecompressedSize, size, 0);
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
        return bigEndian ? Short.reverseBytes(result) : result;
    }

    @Override
    public int readInt() throws IOException {
        readBytes(scratch.slice(0, Integer.BYTES));
        var result = scratch.getInt(0);
        return bigEndian ? Integer.reverseBytes(result) : result;
    }

    @Override
    public long readLong() throws IOException {
        readBytes(scratch.slice(0, Long.BYTES));
        var result = scratch.getLong(0);
        return bigEndian ? Long.reverseBytes(result) : result;
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

                reader.position(chunk.compressedOffset());
                reader.readBytes(compressedSlice);
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
    public BinaryReader position(long pos) {
        position = Check.position(pos, size(), "position");
        return this;
    }

    @Override
    public BinaryReader slice(long offset, long length) {
        Check.fromIndexSize(offset, length, size());
        long absoluteOffset = this.offset + offset;
        if (length == 0) {
            return new SliceBinaryReader<>(reader, chunks, compressed.length(), decompressed.length(), 0, absoluteOffset, this);
        }
        var floor = chunks.floorKey(absoluteOffset);
        if (floor == null) {
            throw new IllegalArgumentException("No chunk found for offset " + offset);
        }
        var ceiling = chunks.ceilingKey(absoluteOffset + length);
        var slice = ceiling != null
            ? chunks.subMap(floor, true, ceiling, false)
            : chunks.tailMap(floor, true);
        return new SliceBinaryReader<>(reader, slice, compressed.length(), decompressed.length(), length, absoluteOffset, this);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    protected abstract void decompress(Bytes.Mutable src, Bytes.Mutable dst, T chunk);

    private static final class SliceBinaryReader<T extends Chunk> extends ChunkedBinaryReader<T> {
        final ChunkedBinaryReader<T> delegate;

        SliceBinaryReader(
            BinaryReader reader,
            NavigableMap<Long, T> chunks,
            int maxCompressedSize,
            int maxDecompressedSize,
            long size,
            long baseOffset,
            ChunkedBinaryReader<T> delegate
        ) {
            super(reader, chunks, maxCompressedSize, maxDecompressedSize, size, baseOffset);
            this.delegate = delegate;
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
