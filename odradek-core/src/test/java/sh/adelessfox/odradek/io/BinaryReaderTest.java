package sh.adelessfox.odradek.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.reversed.toolbox.collect.Bytes;

import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BinaryReaderTest {
    private static final int BUFFER_SIZE = 0x2000;

    @TempDir
    Path directory;

    @Test
    void bulkReadAfterExhaustingBuffer() throws IOException {
        var data = data(BUFFER_SIZE * 2);
        try (var reader = open(data)) {
            reader.readByte();
            reader.readBytes(BUFFER_SIZE - 1);
            assertArrayEquals(Arrays.copyOfRange(data, BUFFER_SIZE, data.length), reader.readBytes(BUFFER_SIZE));
            assertEquals(data.length, reader.position());
        }
    }

    @Test
    void fileSliceStopsAtItsEnd() throws IOException {
        var data = data(32);
        try (var reader = open(data); var slice = reader.slice(10, 2)) {
            assertEquals(data[10], slice.readByte());
            assertThrows(EOFException.class, slice::readShort);
            assertThrows(EOFException.class, () -> slice.readBytes(2));
            assertEquals(1, slice.position());
            assertEquals(data[11], slice.readByte());
            assertThrows(EOFException.class, slice::readByte);
        }
    }

    @Test
    void chunkedSlicesHaveRelativePositionsAndBounds() throws IOException {
        var data = data(48);
        try (var reader = chunked(data);
             var slice = reader.slice(17, 20);
             var nested = slice.slice(3, 12)
        ) {
            assertEquals(0, slice.position());
            assertEquals(20, slice.remaining());
            assertEquals(0, nested.position());
            assertThrows(EOFException.class, () -> nested.readBytes(13));
            nested.position(11);
            assertThrows(EOFException.class, nested::readShort);
            nested.position(0);
            assertArrayEquals(Arrays.copyOfRange(data, 20, 32), nested.readBytes(12));
            assertEquals(0, nested.remaining());
            assertThrows(EOFException.class, nested::readByte);
            assertEquals(0, reader.position());
            assertEquals(0, slice.position());
        }
    }

    @Test
    void finalChunkAndEmptySlicesDoNotOwnTheFile() throws IOException {
        var data = data(48);
        try (var reader = chunked(data)) {
            try (var whole = reader.slice(0, data.length);
                 var empty = whole.slice(data.length, 0)) {
                assertArrayEquals(data, whole.readBytes(data.length));
                assertArrayEquals(new byte[0], empty.readBytes(0));
                assertThrows(EOFException.class, empty::readByte);
            }
            reader.position(32);
            assertArrayEquals(Arrays.copyOfRange(data, 32, 48), reader.readBytes(16));
        }
    }

    @Test
    void primitiveAndBulkReadsAccumulateShortReads() throws IOException {
        var data = data(BUFFER_SIZE * 2);
        var reader = new BufferedBinaryReader(data.length) {
            @Override
            int readImpl(Bytes.Mutable target, long position) {
                int length = (int) Math.min(3, Math.min(target.length(), data.length - position));
                Bytes.wrap(data, (int) position, length).copyTo(target, 0);
                return length;
            }

            @Override
            public void close() {
            }
        };
        try (reader) {
            assertEquals(Bytes.wrap(data).getLong(0), reader.readLong());
            assertArrayEquals(Arrays.copyOfRange(data, 8, data.length), reader.readBytes(data.length - 8));
            assertEquals(data.length, reader.position());
        }
    }

    private BinaryReader open(byte[] data) throws IOException {
        return BinaryReader.open(Files.write(directory.resolve("reader.bin"), data));
    }

    private BinaryReader chunked(byte[] data) throws IOException {
        return new ChunkedBinaryReader<>(open(data), List.of(new TestChunk(0), new TestChunk(16), new TestChunk(32))) {
            @Override
            protected void decompress(Bytes.Mutable src, Bytes.Mutable dst, TestChunk chunk) {
                src.copyTo(dst, 0);
            }
        };
    }

    private static byte[] data(int length) {
        byte[] data = new byte[length];
        new Random(42).nextBytes(data);
        return data;
    }

    private record TestChunk(long offset) implements ChunkedBinaryReader.Chunk {
        @Override
        public int size() {
            return 16;
        }

        @Override
        public long compressedOffset() {
            return offset;
        }

        @Override
        public int compressedSize() {
            return size();
        }
    }
}
