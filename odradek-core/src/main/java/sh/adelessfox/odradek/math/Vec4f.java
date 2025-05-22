package sh.adelessfox.odradek.math;

public record Vec4f(float x, float y, float z, float w) {
    public Vec4f mul(Mat4f other) {
        var x = Math.fma(other.m00(), x(), Math.fma(other.m10(), y(), Math.fma(other.m20(), z(), other.m30() * w())));
        var y = Math.fma(other.m01(), x(), Math.fma(other.m11(), y(), Math.fma(other.m21(), z(), other.m31() * w())));
        var z = Math.fma(other.m02(), x(), Math.fma(other.m12(), y(), Math.fma(other.m22(), z(), other.m32() * w())));
        var w = Math.fma(other.m03(), x(), Math.fma(other.m13(), y(), Math.fma(other.m23(), z(), other.m33() * w())));

        return new Vec4f(x, y, z, w);
    }
}
