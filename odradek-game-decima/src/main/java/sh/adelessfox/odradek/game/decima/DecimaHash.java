package sh.adelessfox.odradek.game.decima;

import wtf.reversed.toolbox.hash.CRCAlgorithm;
import wtf.reversed.toolbox.hash.HashFunction;

public final class DecimaHash {
    private static final HashFunction CRC32 = HashFunction.crc(new CRCAlgorithm(32, 0x1edc6f41, 0, true, true, 0));
    private static final HashFunction MURMUR3 = HashFunction.murmur3(42);

    private DecimaHash() {
    }

    public static HashFunction murmur3() {
        return MURMUR3;
    }

    public static HashFunction crc32() {
        return CRC32;
    }
}
