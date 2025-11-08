package sh.adelessfox.odradek.math;

/**
 * Contains the definition and functions for rotations expressed as immutable 4-dimensional vectors.
 *
 * @param x the real/scalar part of the quaternion
 * @param y the first component of the vector part
 * @param z the second component of the vector part
 * @param w the third component of the vector part
 */
public record Quaternionf(float x, float y, float z, float w) {
    public static final int BYTES = Float.BYTES * 4;

    private static final Quaternionf identity = new Quaternionf(0, 0, 0, 1);

    public static Quaternionf identity() {
        return identity;
    }

    static Quaternionf fromMatrix(
        float m00, float m01, float m02,
        float m10, float m11, float m12,
        float m20, float m21, float m22
    ) {
        float xLength = (float) (1.0f / Math.sqrt(m00 * m00 + m01 * m01 + m02 * m02));
        float yLength = (float) (1.0f / Math.sqrt(m10 * m10 + m11 * m11 + m12 * m12));
        float zLength = (float) (1.0f / Math.sqrt(m20 * m20 + m21 * m21 + m22 * m22));

        return fromMatrixNormalized(
            m00 * xLength, m01 * xLength, m02 * xLength,
            m10 * yLength, m11 * yLength, m12 * yLength,
            m20 * zLength, m21 * zLength, m22 * zLength
        );
    }

    static Quaternionf fromMatrixNormalized(
        float m00, float m01, float m02,
        float m10, float m11, float m12,
        float m20, float m21, float m22
    ) {
        if (m22 <= 0.0f) { // x^2 + y^2 >= z^2 + w^2
            float dif10 = m11 - m00;
            float omm22 = 1.0f - m22;
            if (dif10 <= 0.0f) { // x^2 >= y^2
                float four_xsq = omm22 - dif10;
                float inv4x = (float) (0.5f / Math.sqrt(four_xsq));
                return new Quaternionf(
                    (four_xsq) * inv4x,
                    (m01 + m10) * inv4x,
                    (m02 + m20) * inv4x,
                    (m12 - m21) * inv4x
                );
            } else { // y^2 >= x^2
                float four_ysq = omm22 + dif10;
                float inv4y = (float) (0.5f / Math.sqrt(four_ysq));
                return new Quaternionf(
                    (m01 + m10) * inv4y,
                    (four_ysq) * inv4y,
                    (m12 + m21) * inv4y,
                    (m20 - m02) * inv4y
                );
            }
        } else { // z^2 + w^2 >= x^2 + y^2
            float sum10 = m11 + m00;
            float opm22 = 1.0f + m22;
            if (sum10 <= 0.0f) { // z^2 >= w^2
                float four_zsq = opm22 - sum10;
                float inv4z = (float) (0.5f / Math.sqrt(four_zsq));
                return new Quaternionf(
                    (m02 + m20) * inv4z,
                    (m12 + m21) * inv4z,
                    (four_zsq) * inv4z,
                    (m01 - m10) * inv4z
                );
            } else { // w^2 >= z^2
                float four_wsq = opm22 + sum10;
                float inv4w = (float) (0.5f / Math.sqrt(four_wsq));
                return new Quaternionf(
                    (m12 - m21) * inv4w,
                    (m20 - m02) * inv4w,
                    (m01 - m10) * inv4w,
                    (four_wsq) * inv4w
                );
            }
        }
    }

    public Quaternionf multiply(float scalar) {
        return new Quaternionf(x * scalar, y * scalar, z * scalar, w * scalar);
    }
}
