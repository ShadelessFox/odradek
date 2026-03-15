package sh.adelessfox.odradek.compression;

import java.nio.ByteBuffer;

public abstract sealed class Compressor
    permits LZ4Compressor {

    Compressor() {
    }

    public static Compressor lz4() {
        return new LZ4Compressor();
    }

    public abstract void compress(ByteBuffer src, ByteBuffer dst);

    /**
     * Returns the maximum compressed length for an input of size {@code length}.
     *
     * @param length the input size in bytes
     * @return the maximum compressed length in bytes
     */
    public abstract int maxCompressedLength(int length);
}
