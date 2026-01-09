package sh.adelessfox.atrac9j;


import sh.adelessfox.atrac9j.util.Helpers;

import java.util.Objects;

record HuffmanCodebook(
    short[] codes,
    byte[] bits,
    byte[] lookup,
    int valueCount,
    int valueCountPower,
    int valueBits,
    int valueMax,
    int maxBitSize
) {
    static HuffmanCodebook of(short[] codes, byte[] bits, byte valueCountPower) {
        Objects.requireNonNull(codes, "codes");
        Objects.requireNonNull(bits, "bits");

        var valueCount = 1 << valueCountPower;
        var valueBits = Helpers.log2(codes.length) >>> valueCountPower;
        var valueMax = 1 << valueBits;

        int max = 0;
        for (byte bitSize : bits) {
            max = Math.max(max, bitSize);
        }

        var maxBitSize = max;
        var lookup = createLookupTable(bits, codes, max);

        return new HuffmanCodebook(
            codes,
            bits,
            lookup,
            valueCount,
            valueCountPower,
            valueBits,
            valueMax,
            maxBitSize
        );
    }

    private static byte[] createLookupTable(byte[] bits, short[] codes, int maxBitSize) {
        if (codes == null || bits == null) {
            return null;
        }

        int tableSize = 1 << maxBitSize;
        var dest = new byte[tableSize];

        for (int i = 0; i < bits.length; i++) {
            if (bits[i] == 0) {
                continue;
            }

            int unusedBits = maxBitSize - bits[i];
            int start = codes[i] << unusedBits;
            int length = 1 << unusedBits;
            int end = start + length;

            for (int j = start; j < end; j++) {
                dest[j] = (byte) i;
            }
        }

        return dest;
    }
}

