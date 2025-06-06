package sh.adelessfox.odradek.hashing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class Murmur3Function extends HashFunction {
    static final HashFunction MURMUR3 = new Murmur3Function(42);

    private final int seed;

    Murmur3Function(int seed) {
        this.seed = seed;
    }

    @Override
    public HashCode hash(byte[] input, int off, int len) {
        return HashCode.fromBytes(mmh3(input, off, len, seed));
    }

    private static byte[] mmh3(byte[] data, int off, int len, long seed) {
        var src = ByteBuffer.wrap(data, off, len).order(ByteOrder.LITTLE_ENDIAN);
        var dst = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);

        var h1 = seed;
        var h2 = seed;

        while (src.remaining() >= 16) {
            h1 = (Long.rotateLeft(h1 ^ mixK1(src.getLong()), 27) + h2) * 5 + 0x52dce729;
            h2 = (Long.rotateLeft(h2 ^ mixK2(src.getLong()), 31) + h1) * 5 + 0x38495ab5;
        }

        if (src.hasRemaining()) {
            dst.put(src);
            h1 ^= mixK1(dst.getLong(0));
            h2 ^= mixK2(dst.getLong(8));
        }

        h1 ^= len;
        h2 ^= len;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return dst.putLong(0, h1).putLong(8, h2).array();
    }

    private static long mixK1(long k1) {
        return Long.rotateLeft(k1 * 0x87c37b91114253d5L, 31) * 0x4cf5ad432745937fL;
    }

    private static long mixK2(long k2) {
        return Long.rotateLeft(k2 * 0x4cf5ad432745937fL, 33) * 0x87c37b91114253d5L;
    }

    private static long fmix64(long hash) {
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdL;
        hash ^= (hash >>> 33);
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= (hash >>> 33);
        return hash;
    }
}
