package sh.adelessfox.odradek.math;

/**
 * Contains the definition of an immutable vector comprising 4 floats and associated transformations.
 *
 * @param x the x component of the vector
 * @param y the y component of the vector
 * @param z the z component of the vector
 * @param w the w component of the vector
 */
public record Vector4f(float x, float y, float z, float w) {
    public static final int BYTES = Float.BYTES * 4;

    public Vector4f transform(Matrix4f other) {
        var x = Math.fma(other.m00(), x(), Math.fma(other.m10(), y(), Math.fma(other.m20(), z(), other.m30() * w())));
        var y = Math.fma(other.m01(), x(), Math.fma(other.m11(), y(), Math.fma(other.m21(), z(), other.m31() * w())));
        var z = Math.fma(other.m02(), x(), Math.fma(other.m12(), y(), Math.fma(other.m22(), z(), other.m32() * w())));
        var w = Math.fma(other.m03(), x(), Math.fma(other.m13(), y(), Math.fma(other.m23(), z(), other.m33() * w())));

        return new Vector4f(x, y, z, w);
    }

    public Vector4f add(Vector4f other) {
        return add(other.x(), other.y(), other.z(), other.w());
    }

    public Vector4f add (float x, float y, float z, float w) {
        return new Vector4f(x() + x,  y() + y, z() + z, w() + w);
    }

    public Vector4f sub(Vector4f other) {
        return sub(other.x(), other.y(), other.z(), other.w());
    }

    public Vector4f sub(float x, float y, float z, float w) {
        return new Vector4f(x() - x,  y() - y, z() - z, w() - w);
    }
}
