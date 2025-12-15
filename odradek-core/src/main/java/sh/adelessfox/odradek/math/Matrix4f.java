package sh.adelessfox.odradek.math;

import java.nio.FloatBuffer;

/**
 * Contains the definition of an immutable 4x4 Matrix of floats, and associated functions to transform it.
 */
public record Matrix4f(
    float m00, float m01, float m02, float m03,
    float m10, float m11, float m12, float m13,
    float m20, float m21, float m22, float m23,
    float m30, float m31, float m32, float m33
) {
    public static final int BYTES = Float.BYTES * 16;

    private static final Matrix4f identity = new Matrix4f(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1
    );

    public static Matrix4f identity() {
        return identity;
    }

    public static Matrix4f perspective(float fov, float aspect, float near, float far) {
        float h = (float) Math.tan(fov * 0.5f);
        float m00 = 1.0f / (h * aspect);
        float m11 = 1.0f / h;
        float m22 = (far + near) / (near - far);
        float m32 = (far + far) * near / (near - far);
        float m23 = -1.0f;

        return new Matrix4f(
            m00, 0.f, 0.f, 0.f,
            0.f, m11, 0.f, 0.f,
            0.f, 0.f, m22, m23,
            0.f, 0.f, m32, 0.f
        );
    }

    public static Matrix4f rotation(Quaternionf q) {
        float rm00 = q.w() * q.w() + q.x() * q.x() - q.z() * q.z() - q.y() * q.y();
        float rm01 = q.x() * q.y() + q.x() * q.y() + q.z() * q.w() + q.z() * q.w();
        float rm02 = q.x() * q.z() + q.x() * q.z() - q.y() * q.w() - q.y() * q.w();
        float rm10 = q.x() * q.y() - q.z() * q.w() - q.z() * q.w() + q.x() * q.y();
        float rm11 = q.y() * q.y() - q.z() * q.z() + q.w() * q.w() - q.x() * q.x();
        float rm12 = q.y() * q.z() + q.y() * q.z() + q.x() * q.w() + q.x() * q.w();
        float rm20 = q.y() * q.w() + q.y() * q.w() + q.x() * q.z() + q.x() * q.z();
        float rm21 = q.y() * q.z() + q.y() * q.z() - q.x() * q.w() - q.x() * q.w();
        float rm22 = q.z() * q.z() - q.y() * q.y() - q.x() * q.x() + q.w() * q.w();

        return new Matrix4f(
            rm00, rm01, rm02, 0.0f,
            rm10, rm11, rm12, 0.0f,
            rm20, rm21, rm22, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        );
    }

    public static Matrix4f translation(float x, float y, float z) {
        return new Matrix4f(
            1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            x, y, z, 1.f
        );
    }

    public static Matrix4f ortho2D(float left, float right, float bottom, float top) {
        float m00 = 2.0f / (right - left);
        float m11 = 2.0f / (top - bottom);
        float m22 = -1.0f;
        float m30 = (right + left) / (left - right);
        float m31 = (top + bottom) / (bottom - top);

        return new Matrix4f(
            m00, 0.f, 0.f, 0.f,
            0.f, m11, 0.f, 0.f,
            0.f, 0.f, m22, 0.f,
            m30, m31, 0.f, 1.f
        );
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f center, Vector3f up) {
        Vector3f dir = eye.sub(center).normalize();
        Vector3f left = up.cross(dir).normalize();
        Vector3f upn = dir.cross(left);

        Matrix4f result = new Matrix4f(
            left.x(), upn.x(), dir.x(), 0.f,
            left.y(), upn.y(), dir.y(), 0.f,
            left.z(), upn.z(), dir.z(), 0.f,
            0.f, 0.f, 0.f, 1.f
        );

        return result.translate(-eye.x(), -eye.y(), -eye.z());
    }

    public Matrix4f rotate(Quaternionf q) {
        return mul(rotation(q));
    }

    public Matrix4f translate(float x, float y, float z) {
        float m30 = Math.fma(m00(), x, Math.fma(m10(), y, Math.fma(m20(), z, m30())));
        float m31 = Math.fma(m01(), x, Math.fma(m11(), y, Math.fma(m21(), z, m31())));
        float m32 = Math.fma(m02(), x, Math.fma(m12(), y, Math.fma(m22(), z, m32())));
        float m33 = Math.fma(m03(), x, Math.fma(m13(), y, Math.fma(m23(), z, m33())));

        return new Matrix4f(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33
        );
    }

    public Matrix4f scale(float xyz) {
        return scale(xyz, xyz, xyz);
    }

    public Matrix4f scale(float x, float y, float z) {
        return new Matrix4f(
            m00 * x, m01 * x, m02 * x, m03 * x,
            m10 * y, m11 * y, m12 * y, m13 * y,
            m20 * z, m21 * z, m22 * z, m23 * z,
            m30, m31, m32, m33
        );
    }

    public Matrix4f mul(Matrix4f o) {
        var m00 = Math.fma(m00(), o.m00(), Math.fma(m10(), o.m01(), Math.fma(m20(), o.m02(), m30() * o.m03())));
        var m01 = Math.fma(m01(), o.m00(), Math.fma(m11(), o.m01(), Math.fma(m21(), o.m02(), m31() * o.m03())));
        var m02 = Math.fma(m02(), o.m00(), Math.fma(m12(), o.m01(), Math.fma(m22(), o.m02(), m32() * o.m03())));
        var m03 = Math.fma(m03(), o.m00(), Math.fma(m13(), o.m01(), Math.fma(m23(), o.m02(), m33() * o.m03())));
        var m10 = Math.fma(m00(), o.m10(), Math.fma(m10(), o.m11(), Math.fma(m20(), o.m12(), m30() * o.m13())));
        var m11 = Math.fma(m01(), o.m10(), Math.fma(m11(), o.m11(), Math.fma(m21(), o.m12(), m31() * o.m13())));
        var m12 = Math.fma(m02(), o.m10(), Math.fma(m12(), o.m11(), Math.fma(m22(), o.m12(), m32() * o.m13())));
        var m13 = Math.fma(m03(), o.m10(), Math.fma(m13(), o.m11(), Math.fma(m23(), o.m12(), m33() * o.m13())));
        var m20 = Math.fma(m00(), o.m20(), Math.fma(m10(), o.m21(), Math.fma(m20(), o.m22(), m30() * o.m23())));
        var m21 = Math.fma(m01(), o.m20(), Math.fma(m11(), o.m21(), Math.fma(m21(), o.m22(), m31() * o.m23())));
        var m22 = Math.fma(m02(), o.m20(), Math.fma(m12(), o.m21(), Math.fma(m22(), o.m22(), m32() * o.m23())));
        var m23 = Math.fma(m03(), o.m20(), Math.fma(m13(), o.m21(), Math.fma(m23(), o.m22(), m33() * o.m23())));
        var m30 = Math.fma(m00(), o.m30(), Math.fma(m10(), o.m31(), Math.fma(m20(), o.m32(), m30() * o.m33())));
        var m31 = Math.fma(m01(), o.m30(), Math.fma(m11(), o.m31(), Math.fma(m21(), o.m32(), m31() * o.m33())));
        var m32 = Math.fma(m02(), o.m30(), Math.fma(m12(), o.m31(), Math.fma(m22(), o.m32(), m32() * o.m33())));
        var m33 = Math.fma(m03(), o.m30(), Math.fma(m13(), o.m31(), Math.fma(m23(), o.m32(), m33() * o.m33())));

        return new Matrix4f(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33
        );
    }

    public Matrix4f invert() {
        var a = m00() * m11() - m01() * m10();
        var b = m00() * m12() - m02() * m10();
        var c = m00() * m13() - m03() * m10();
        var d = m01() * m12() - m02() * m11();
        var e = m01() * m13() - m03() * m11();
        var f = m02() * m13() - m03() * m12();
        var g = m20() * m31() - m21() * m30();
        var h = m20() * m32() - m22() * m30();
        var i = m20() * m33() - m23() * m30();
        var j = m21() * m32() - m22() * m31();
        var k = m21() * m33() - m23() * m31();
        var l = m22() * m33() - m23() * m32();

        var determinant = a * l - b * k + c * j + d * i - e * h + f * g;
        var scale = 1.0f / determinant;

        var m00 = Math.fma(m11(), l, Math.fma(-m12(), k, m13() * j)) * scale;
        var m01 = Math.fma(-m01(), l, Math.fma(m02(), k, -m03() * j)) * scale;
        var m02 = Math.fma(m31(), f, Math.fma(-m32(), e, m33() * d)) * scale;
        var m03 = Math.fma(-m21(), f, Math.fma(m22(), e, -m23() * d)) * scale;
        var m10 = Math.fma(-m10(), l, Math.fma(m12(), i, -m13() * h)) * scale;
        var m11 = Math.fma(m00(), l, Math.fma(-m02(), i, m03() * h)) * scale;
        var m12 = Math.fma(-m30(), f, Math.fma(m32(), c, -m33() * b)) * scale;
        var m13 = Math.fma(m20(), f, Math.fma(-m22(), c, m23() * b)) * scale;
        var m20 = Math.fma(m10(), k, Math.fma(-m11(), i, m13() * g)) * scale;
        var m21 = Math.fma(-m00(), k, Math.fma(m01(), i, -m03() * g)) * scale;
        var m22 = Math.fma(m30(), e, Math.fma(-m31(), c, m33() * a)) * scale;
        var m23 = Math.fma(-m20(), e, Math.fma(m21(), c, -m23() * a)) * scale;
        var m30 = Math.fma(-m10(), j, Math.fma(m11(), h, -m12() * g)) * scale;
        var m31 = Math.fma(m00(), j, Math.fma(-m01(), h, m02() * g)) * scale;
        var m32 = Math.fma(-m30(), d, Math.fma(m31(), b, -m32() * a)) * scale;
        var m33 = Math.fma(m20(), d, Math.fma(-m21(), b, m22() * a)) * scale;

        return new Matrix4f(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33
        );
    }

    public Vector3f toTranslation() {
        return new Vector3f(m30, m31, m32);
    }

    public Vector3f toScale() {
        float x = (float) Math.sqrt(m00 * m00 + m01 * m01 + m02 * m02);
        float y = (float) Math.sqrt(m10 * m10 + m11 * m11 + m12 * m12);
        float z = (float) Math.sqrt(m20 * m20 + m21 * m21 + m22 * m22);
        return new Vector3f(x, y, z);
    }

    public Quaternionf toRotation() {
        return Quaternionf.fromMatrix(
            m00, m01, m02,
            m10, m11, m12,
            m20, m21, m22
        );
    }

    public Vector4f get(int column) {
        return switch (column) {
            case 0 -> new Vector4f(m00, m10, m20, m30);
            case 1 -> new Vector4f(m01, m11, m21, m31);
            case 2 -> new Vector4f(m02, m12, m22, m32);
            case 3 -> new Vector4f(m03, m13, m23, m33);
            default -> throw new IllegalArgumentException("Column must be in range 0, 3");
        };
    }

    public FloatBuffer get(FloatBuffer dst) {
        dst.put(m00).put(m01).put(m02).put(m03);
        dst.put(m10).put(m11).put(m12).put(m13);
        dst.put(m20).put(m21).put(m22).put(m23);
        dst.put(m30).put(m31).put(m32).put(m33);
        return dst;
    }

    @Override
    public String toString() {
        return "(" + m00 + ", " + m10 + ", " + m20 + ", " + m30 + ")\n" +
            "(" + m01 + ", " + m11 + ", " + m21 + ", " + m31 + ")\n" +
            "(" + m02 + ", " + m12 + ", " + m22 + ", " + m32 + ")\n" +
            "(" + m03 + ", " + m13 + ", " + m23 + ", " + m33 + ")";
    }
}
