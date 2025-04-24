package sh.adelessfox.odradek.compression;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract sealed class Decompressor
    permits LZ4Decompressor, NoneDecompressor {

    public static Decompressor none() {
        return new NoneDecompressor();
    }

    public static Decompressor lz4() {
        return new LZ4Decompressor();
    }

    public abstract void decompress(ByteBuffer src, ByteBuffer dst) throws IOException;

    public void decompress(byte[] src, int srcLen, byte[] dst, int dstLen) throws IOException {
        decompress(src, 0, srcLen, dst, 0, dstLen);
    }

    public void decompress(byte[] src, int srcOff, int srcLen, byte[] dst, int dstOff, int dstLen) throws IOException {
        decompress(ByteBuffer.wrap(src, srcOff, srcLen), ByteBuffer.wrap(dst, dstOff, dstLen));
    }
}
