package sh.adelessfox.odradek.compression;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.nio.ByteBuffer;

final class LZ4Decompressor extends Decompressor {
    private static final LZ4FastDecompressor decompressor = LZ4Factory.fastestInstance().fastDecompressor();

    @Override
    public void decompress(ByteBuffer src, ByteBuffer dst) {
        decompressor.decompress(src, dst);
    }
}
