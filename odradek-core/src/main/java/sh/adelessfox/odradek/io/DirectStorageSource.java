package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.compress.Decompressor;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * A source for DirectStorage archives.
 *
 * @see <a href="https://github.com/ShadelessFox/decima/wiki/Archives#directstorage-archive">DirectStorage archive format</a>
 */
public final class DirectStorageSource extends ChunkedBinarySource<DirectStorageSource.Chunk> {
    private DirectStorageSource(BinarySource source, List<Chunk> chunks) {
        super(source, chunks);
    }

    public static BinarySource open(Path path) throws IOException {
        var source = BinarySource.open(path);
        try {
            var header = Header.read(source);
            var chunks = source.readObjects(header.chunkCount(), Chunk::read);
            return new DirectStorageSource(source, chunks);
        } catch (IOException e) {
            source.close(); // don't want to leak the file handle if we fail to read the header
            throw e;
        }
    }

    @Override
    protected void decompress(Bytes.Mutable src, Bytes.Mutable dst, Chunk chunk) {
        Decompressor.lz4Block().decompress(src, dst);
    }

    @Override
    public String toString() {
        return "DirectStorageSource[position=" + position() + ", size=" + size() + "]";
    }

    private record Header(
        int magic,
        short versionMajor,
        short versionMinor,
        int chunkCount,
        int firstChunkOffset,
        long totalSize
    ) {
        static final int MAGIC = 'D' | 'S' << 8 | 'A' << 16 | 'R' << 24;

        static Header read(BinarySource source) throws IOException {
            var magic = source.readInt();
            var versionMajor = source.readShort();
            var versionMinor = source.readShort();
            var chunkCount = source.readInt();
            var firstChunkOffset = source.readInt();
            var totalSize = source.readLong();
            source.skip(8); // padding

            if (magic != MAGIC) {
                throw new IOException("Invalid archive magic: %08x".formatted(magic));
            }

            if (versionMajor != 3 && versionMinor != 1) {
                throw new IOException("Unsupported archive version: %d.%d".formatted(versionMajor, versionMinor));
            }

            return new Header(magic, versionMajor, versionMinor, chunkCount, firstChunkOffset, totalSize);
        }
    }

    public record Chunk(
        long offset,
        long compressedOffset,
        int size,
        int compressedSize,
        byte type
    ) implements ChunkedBinarySource.Chunk {
        static Chunk read(BinarySource source) throws IOException {
            var offset = source.readLong();
            var compressedOffset = source.readLong();
            var size = source.readInt();
            var compressedSize = source.readInt();
            var type = source.readByte();
            source.skip(7); // padding

            return new Chunk(offset, compressedOffset, size, compressedSize, type);
        }
    }
}
