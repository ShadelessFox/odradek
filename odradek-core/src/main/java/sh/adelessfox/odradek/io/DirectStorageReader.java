package sh.adelessfox.odradek.io;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.compress.Decompressor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * A reader for DirectStorage archives.
 *
 * @see <a href="https://github.com/ShadelessFox/decima/wiki/Archives#directstorage-archive">DirectStorage archive format</a>
 */
public final class DirectStorageReader extends ChunkedBinaryReader<DirectStorageReader.Chunk> {
    private DirectStorageReader(BinaryReader reader, List<Chunk> chunks) {
        super(reader, chunks);
    }

    public static BinaryReader open(Path path) throws IOException {
        var reader = BinaryReader.open(path);
        try {
            var header = Header.read(reader);
            var chunks = reader.readObjects(header.chunkCount(), Chunk::read);
            return new DirectStorageReader(reader, chunks);
        } catch (IOException e) {
            reader.close(); // don't want to leak the file handle if we fail to read the header
            throw e;
        }
    }

    @Override
    protected void decompress(Bytes.Mutable src, Bytes.Mutable dst, Chunk chunk) {
        Decompressor.lz4Block().decompress(
            src.slice(0, chunk.compressedSize()),
            dst.slice(0, chunk.size()));
    }

    @Override
    public String toString() {
        return "DirectStorageReader[position=" + position() + ", size=" + size() + "]";
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

        static Header read(BinaryReader reader) throws IOException {
            var magic = reader.readInt();
            var versionMajor = reader.readShort();
            var versionMinor = reader.readShort();
            var chunkCount = reader.readInt();
            var firstChunkOffset = reader.readInt();
            var totalSize = reader.readLong();
            reader.skip(8); // padding

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
    ) implements ChunkedBinaryReader.Chunk {
        static Chunk read(BinaryReader reader) throws IOException {
            var offset = reader.readLong();
            var compressedOffset = reader.readLong();
            var size = reader.readInt();
            var compressedSize = reader.readInt();
            var type = reader.readByte();
            reader.skip(7); // padding

            return new Chunk(offset, compressedOffset, size, compressedSize, type);
        }
    }
}
