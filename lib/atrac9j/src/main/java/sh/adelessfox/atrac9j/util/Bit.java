package sh.adelessfox.atrac9j.util;

public final class Bit {
    private Bit() {
    }

    public static int bitReverse32(int value, int bitCount) {
        return Integer.reverse(value) >>> (32 - bitCount);
    }

    public static int signExtend32(int value, int bits) {
        int shift = Integer.SIZE - bits;
        return (value << shift) >> shift;
    }
}

