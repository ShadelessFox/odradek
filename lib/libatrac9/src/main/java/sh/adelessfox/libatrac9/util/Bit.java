package sh.adelessfox.libatrac9.util;

public final class Bit {
    private Bit() {
    }

    public static int BitReverse32(int value) {
        value = ((value & 0xaaaaaaaa) >>> 1) | ((value & 0x55555555) << 1);
        value = ((value & 0xcccccccc) >>> 2) | ((value & 0x33333333) << 2);
        value = ((value & 0xf0f0f0f0) >>> 4) | ((value & 0x0f0f0f0f) << 4);
        value = ((value & 0xff00ff00) >>> 8) | ((value & 0x00ff00ff) << 8);
        return (value >>> 16) | (value << 16);
    }

    public static int BitReverse32(int value, int bitCount) {
        return BitReverse32(value) >>> (32 - bitCount);
    }

    public static int SignExtend32(int value, int bits) {
        int shift = Integer.SIZE - bits;
        return (value << shift) >> shift;
    }
}

