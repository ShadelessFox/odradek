package sh.adelessfox.odradek.game.decima;

import wtf.reversed.toolbox.hash.CRCAlgorithm;
import wtf.reversed.toolbox.hash.HashFunction;

public final class DecimaHash {
    private static final CRCAlgorithm CRC_ALGORITHM = new CRCAlgorithm(32, 0x1edc6f41, 0, true, true, 0);

    private DecimaHash() {
    }

    public static HashFunction murmur3() {
        return HashFunction.murmur3(42);
    }

    public static HashFunction crc32() {
        return HashFunction.crc(CRC_ALGORITHM);
    }
}
