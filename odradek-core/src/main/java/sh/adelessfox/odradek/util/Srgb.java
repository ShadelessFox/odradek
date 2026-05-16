package sh.adelessfox.odradek.util;

/**
 * Utility class for dealing with sRGB conversions.
 * <p>
 * Ripped right from valen, licensed under MIT license, by Jan De Kock.
 */
public final class Srgb {
    private static final int LUT_BITS = 14; // Keeps us within DirectX spec (< 0.6)
    private static final int LUT_MAX = (1 << LUT_BITS) - 1;
    private static final byte[] TO_SRGB = buildToSrgb(LUT_BITS);
    private static final float[] TO_LINEAR = buildToLinear();

    private Srgb() {
    }

    private static byte[] buildToSrgb(int bits) {
        int size = 1 << bits;
        byte[] table = new byte[size];
        for (int i = 0; i < size; i++) {
            table[i] = (byte) Math.fma(linearToSrgb((float) i / (size - 1)), 255.0f, 0.5f);
        }
        return table;
    }

    private static float[] buildToLinear() {
        float[] table = new float[256];
        for (int i = 0; i < 256; i++) {
            table[i] = srgbToLinear((float) i / 255.0f);
        }
        return table;
    }

    /**
     * Converts a linear float value to sRGB. Input should be in {@code [0, 1]}.
     */
    public static float linearToSrgb(float f) {
        if (f <= (0.04045f / 12.92f)) {
            return f * 12.92f;
        } else {
            return Math.fma((float) Math.pow(f, 1.0f / 2.4f), 1.055f, -0.055f);
        }
    }

    /**
     * Converts an sRGB float value to linear. Input should be in {@code [0, 1]}.
     */
    public static float srgbToLinear(float f) {
        if (f <= 0.04045f) {
            return f * (1.0f / 12.92f);
        } else {
            float a = Math.fma(f, 1.0f / 1.055f, 0.055f / 1.055f);
            return (float) Math.pow(a, 2.4f);
        }
    }

    /**
     * Converts a linear float value to an sRGB-encoded byte using a LUT.
     * Values outside {@code [0, 1]} are clamped to 0 or 255.
     */
    public static byte linearToSrgbByte(float f) {
        if (f >= 0.0f && f <= 1.0f) {
            return TO_SRGB[(int) Math.fma(f, LUT_MAX, 0.5f)];
        } else if (f > 1.0f) {
            return (byte) 255;
        } else {
            return 0;
        }
    }

    /**
     * Converts an sRGB-encoded byte to a linear float value using a LUT.
     */
    public static float srgbByteToLinear(byte b) {
        return TO_LINEAR[Byte.toUnsignedInt(b)];
    }
}
