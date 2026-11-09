package sh.adelessfox.odradek.util;

import wtf.reversed.toolbox.util.Check;

import java.util.Arrays;

public sealed class Bits {
    final long[] words;
    final int bitLength;

    private Bits(long[] words, int bitLength) {
        Check.argument(words.length == wordLength(bitLength), "bitLength does not match words.length");
        this.words = words;
        this.bitLength = bitLength;
    }

    Bits(int bitLength) {
        this(new long[wordLength(bitLength)], bitLength);
    }

    public static Mutable allocate(int length) {
        Check.positiveOrZero(length, "length");
        return new Mutable(length);
    }

    public static Bits copyOf(long[] array) {
        return new Bits(array.clone(), Math.multiplyExact(array.length, Long.SIZE));
    }

    public boolean get(int index) {
        Check.index(index, bitLength);
        return getInternal(index);
    }

    private boolean getInternal(int index) {
        return (words[wordIndex(index)] & bitMask(index)) != 0;
    }

    public int length() {
        return bitLength;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof Bits other
            && bitLength == other.bitLength
            && Arrays.equals(words, other.words);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(words) ^ Integer.hashCode(bitLength);
    }

    @Override
    public String toString() {
        return "[" + bitLength + " bits]";
    }

    static long bitMask(int bitIndex) {
        return 1L << bitIndex;
    }

    static int wordIndex(int bitIndex) {
        return bitIndex >> 6;
    }

    static int wordLength(int length) {
        return Math.ceilDivExact(length, Long.SIZE);
    }

    public static final class Mutable extends Bits {
        Mutable(int length) {
            super(length);
        }

        public void set(int index, boolean value) {
            Check.index(index, length());
            if (value) {
                words[wordIndex(index)] |= bitMask(index);
            } else {
                words[wordIndex(index)] &= ~bitMask(index);
            }
        }
    }
}
