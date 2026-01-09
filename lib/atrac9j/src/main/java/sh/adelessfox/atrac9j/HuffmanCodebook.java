package sh.adelessfox.atrac9j;


import sh.adelessfox.atrac9j.util.Helpers;

import java.util.Objects;

final class HuffmanCodebook {
    final short[] Codes;
    final byte[] Bits;
    final byte[] Lookup;
    final int ValueCount;
    final int ValueCountPower;
    final int ValueBits;
    final int ValueMax;
    final int MaxBitSize;

    HuffmanCodebook(short[] codes, byte[] bits, byte valueCountPower) {
        Objects.requireNonNull(codes, "codes");
        Objects.requireNonNull(bits, "bits");

        Codes = codes;
        Bits = bits;

        ValueCount = 1 << valueCountPower;
        ValueCountPower = valueCountPower;
        ValueBits = Helpers.Log2(codes.length) >>> valueCountPower;
        ValueMax = 1 << ValueBits;

        int max = 0;
        for (byte bitSize : bits) {
            max = Math.max(max, bitSize);
        }

        MaxBitSize = max;
        Lookup = CreateLookupTable();
    }

    private byte[] CreateLookupTable() {
        if (Codes == null || Bits == null) {
            return null;
        }

        int tableSize = 1 << MaxBitSize;
        var dest = new byte[tableSize];

        for (int i = 0; i < Bits.length; i++) {
            if (Bits[i] == 0) {
                continue;
            }
            int unusedBits = MaxBitSize - Bits[i];

            int start = Codes[i] << unusedBits;
            int length = 1 << unusedBits;
            int end = start + length;

            for (int j = start; j < end; j++) {
                dest[j] = (byte) i;
            }
        }
        return dest;
    }
}

