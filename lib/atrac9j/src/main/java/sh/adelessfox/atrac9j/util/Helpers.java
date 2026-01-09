package sh.adelessfox.atrac9j.util;

public final class Helpers {
    private Helpers() {
    }

    public static short clamp16(int value) {
        if (value > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        }
        if (value < Short.MIN_VALUE) {
            return Short.MIN_VALUE;
        }
        return (short) value;
    }

    public static int getNextMultiple(int value, int multiple) {
        if (multiple <= 0) {
            return value;
        }

        if (value % multiple == 0) {
            return value;
        }

        return value + multiple - value % multiple;
    }

    /**
     * Returns the floor of the base 2 logarithm of a specified number.
     *
     * @param value The number whose logarithm is to be found.
     * @return The floor of the base 2 logarithm of {@code value}
     */
    public static int log2(int value) {
        value |= value >>> 1;
        value |= value >>> 2;
        value |= value >>> 4;
        value |= value >>> 8;
        value |= value >>> 16;

        return multiplyDeBruijnBitPosition[(value * 0x07C4ACDD) >>> 27];
    }

    private static final int[] multiplyDeBruijnBitPosition = {
        0, 9, 1, 10, 13, 21, 2, 29, 11, 14, 16, 18, 22, 25, 3, 30,
        8, 12, 20, 28, 15, 17, 24, 7, 19, 27, 23, 6, 26, 5, 4, 31
    };
}
