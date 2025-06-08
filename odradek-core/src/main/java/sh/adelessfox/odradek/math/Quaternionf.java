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
}
