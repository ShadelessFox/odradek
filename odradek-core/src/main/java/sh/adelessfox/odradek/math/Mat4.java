package sh.adelessfox.odradek.math;

import java.nio.FloatBuffer;

public record Mat4(
    float m00, float m01, float m02, float m03,
    float m10, float m11, float m12, float m13,
    float m20, float m21, float m22, float m23,
    float m30, float m31, float m32, float m33
) {
    public static final int BYTES = Float.BYTES * 16;

    private static final Mat4 identity = new Mat4(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1
    );

    public static Mat4 identity() {
        return identity;
    }

    public static Mat4 rotationX(float ang) {
        float sin = (float) Math.sin(ang);
        float cos = (float) Math.cos(ang);
        return new Mat4(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, +cos, -sin, 0.0f,
            0.0f, +sin, -cos, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        );
    }

    public static Mat4 perspective(float fov, float aspect, float near, float far) {
        float h = (float) Math.tan(fov * 0.5f);
        float m00 = 1.0f / (h * aspect);
        float m11 = 1.0f / h;
        float m22 = (far + near) / (near - far);
        float m32 = (far + far) * near / (near - far);
        float m23 = -1.0f;

        return new Mat4(
            m00, 0.f, 0.f, 0.f,
            0.f, m11, 0.f, 0.f,
            0.f, 0.f, m22, m23,
            0.f, 0.f, m32, 0.f
        );
    }

    public static Mat4 ortho2D(float left, float right, float bottom, float top) {
        float m00 = 2.0f / (right - left);
        float m11 = 2.0f / (top - bottom);
        float m22 = -1.0f;
        float m30 = (right + left) / (left - right);
        float m31 = (top + bottom) / (bottom - top);

        return new Mat4(
            m00, 0.f, 0.f, 0.f,
            0.f, m11, 0.f, 0.f,
            0.f, 0.f, m22, 0.f,
            m30, m31, 0.f, 1.f
        );
    }

    public static Mat4 lookAt(Vec3 eye, Vec3 center, Vec3 up) {
        Vec3 dir = eye.sub(center).normalize();
        Vec3 left = up.cross(dir).normalize();
        Vec3 upn = dir.cross(left);

        Mat4 result = new Mat4(
            left.x(), upn.x(), dir.x(), 0.f,
            left.y(), upn.y(), dir.y(), 0.f,
            left.z(), upn.z(), dir.z(), 0.f,
            0.f, 0.f, 0.f, 1.f
        );

        return result.translate(-eye.x(), -eye.y(), -eye.z());
    }

    public Mat4 rotate(Quat quat) {
        float w2 = quat.w() * quat.w(), x2 = quat.x() * quat.x();
        float y2 = quat.y() * quat.y(), z2 = quat.z() * quat.z();
        float zw = quat.z() * quat.w(), dzw = zw + zw, xy = quat.x() * quat.y(), dxy = xy + xy;
        float xz = quat.x() * quat.z(), dxz = xz + xz, yw = quat.y() * quat.w(), dyw = yw + yw;
        float yz = quat.y() * quat.z(), dyz = yz + yz, xw = quat.x() * quat.w(), dxw = xw + xw;
        float rm00 = w2 + x2 - z2 - y2;
        float rm01 = dxy + dzw;
        float rm02 = dxz - dyw;
        float rm10 = -dzw + dxy;
        float rm11 = y2 - z2 + w2 - x2;
        float rm12 = dyz + dxw;
        float rm20 = dyw + dxz;
        float rm21 = dyz - dxw;
        float rm22 = z2 - y2 - x2 + w2;
        float nm00 = Math.fma(m00(), rm00, Math.fma(m10(), rm01, m20() * rm02));
        float nm01 = Math.fma(m01(), rm00, Math.fma(m11(), rm01, m21() * rm02));
        float nm02 = Math.fma(m02(), rm00, Math.fma(m12(), rm01, m22() * rm02));
        float nm03 = Math.fma(m03(), rm00, Math.fma(m13(), rm01, m23() * rm02));
        float nm10 = Math.fma(m00(), rm10, Math.fma(m10(), rm11, m20() * rm12));
        float nm11 = Math.fma(m01(), rm10, Math.fma(m11(), rm11, m21() * rm12));
        float nm12 = Math.fma(m02(), rm10, Math.fma(m12(), rm11, m22() * rm12));
        float nm13 = Math.fma(m03(), rm10, Math.fma(m13(), rm11, m23() * rm12));
        float nm20 = Math.fma(m00(), rm20, Math.fma(m10(), rm21, m20() * rm22));
        float nm21 = Math.fma(m01(), rm20, Math.fma(m11(), rm21, m21() * rm22));
        float nm22 = Math.fma(m02(), rm20, Math.fma(m12(), rm21, m22() * rm22));
        float nm23 = Math.fma(m03(), rm20, Math.fma(m13(), rm21, m23() * rm22));

        return new Mat4(
            nm00, nm01, nm02, nm03,
            nm10, nm11, nm12, nm13,
            nm20, nm21, nm22, nm23,
            m30(), m31(), m32(), m33()
        );
    }

    public Mat4 rotateX(float ang) {
        return mul(rotationX(ang));
    }

    public Mat4 translate(float x, float y, float z) {
        float m30 = Math.fma(m00(), x, Math.fma(m10(), y, Math.fma(m20(), z, m30())));
        float m31 = Math.fma(m01(), x, Math.fma(m11(), y, Math.fma(m21(), z, m31())));
        float m32 = Math.fma(m02(), x, Math.fma(m12(), y, Math.fma(m22(), z, m32())));
        float m33 = Math.fma(m03(), x, Math.fma(m13(), y, Math.fma(m23(), z, m33())));

        return new Mat4(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33
        );
    }

    public Mat4 scale(float xyz) {
        return scale(xyz, xyz, xyz);
    }

    public Mat4 scale(float x, float y, float z) {
        return new Mat4(
            m00 * x, m01 * x, m02 * x, m03 * x,
            m10 * y, m11 * y, m12 * y, m13 * y,
            m20 * z, m21 * z, m22 * z, m23 * z,
            m30, m31, m32, m33
        );
    }

    public Mat4 mul(Mat4 o) {
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

        return new Mat4(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33
        );
    }

    public Mat4 invert() {
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

        return new Mat4(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33
        );
    }

    public Vec3 translation() {
        return new Vec3(m30, m31, m32);
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
