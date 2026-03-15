package sh.adelessfox.odradek.compression;

import net.jpountz.lz4.LZ4Factory;

import java.nio.ByteBuffer;

final class LZ4Compressor extends Compressor {
    private static final net.jpountz.lz4.LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();

    @Override
    public void compress(ByteBuffer src, ByteBuffer dst) {
        compressor.compress(src, dst);
    }

    @Override
    public int maxCompressedLength(int length) {
        return compressor.maxCompressedLength(length);
    }
}
