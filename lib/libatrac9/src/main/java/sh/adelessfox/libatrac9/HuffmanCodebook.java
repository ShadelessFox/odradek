package sh.adelessfox.libatrac9;


import sh.adelessfox.libatrac9.util.Helpers;

class HuffmanCodebook {
    public HuffmanCodebook(short[] codes, byte[] bits, byte valueCountPower) {
        Codes = codes;
        Bits = bits;
        if (Codes == null || Bits == null) {
            return;
        }

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

    public short[] Codes;
    public byte[] Bits;
    public byte[] Lookup;
    public int ValueCount;
    public int ValueCountPower;
    public int ValueBits;
    public int ValueMax;
    public int MaxBitSize;
}

