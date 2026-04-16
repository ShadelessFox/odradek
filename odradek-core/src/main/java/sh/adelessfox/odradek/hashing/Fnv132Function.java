package sh.adelessfox.odradek.hashing;

import java.util.Objects;

final class Fnv132Function extends HashFunction {
    static final HashFunction FNV1 = new Fnv132Function();

    private static final int FNV_OFFSET_BASIS = 0x811c9dc5;
    private static final int FNV_PRIME = 0x1000193;

    @Override
    public HashCode hash(byte[] input, int off, int len) {
        Objects.checkFromIndexSize(off, len, input.length);
        int hash = FNV_OFFSET_BASIS;
        for (int i = off; i < len; i++) {
            hash *= FNV_PRIME;
            hash ^= input[i];
        }
        return HashCode.fromInt(hash);
    }
}
