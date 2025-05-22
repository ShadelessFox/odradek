package sh.adelessfox.odradek.math;

public record Vec3f(float x, float y, float z) {
    private static final Vec3f one = new Vec3f(1, 1, 1);

    public static Vec3f one() {
        return one;
    }

    public Vec3f add(Vec3f other) {
        return add(other.x, other.y, other.z);
    }

    public Vec3f add(float x, float y, float z) {
        return new Vec3f(this.x + x, this.y + y, this.z + z);
    }

    public Vec3f sub(Vec3f other) {
        return sub(other.x, other.y, other.z);
    }

    public Vec3f sub(float x, float y, float z) {
        return new Vec3f(this.x - x, this.y - y, this.z - z);
    }

    public Vec3f mul(float scalar) {
        return mul(scalar, scalar, scalar);
    }

    public Vec3f mul(Vec3f other) {
        return mul(other.x, other.y, other.z);
    }

    public Vec3f mul(float x, float y, float z) {
        return new Vec3f(this.x * x, this.y * y, this.z * z);
    }

    public float distance(Vec3f other) {
        return distance(other.x, other.y, other.z);
    }

    public float distance(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return (float) Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, dz * dz)));
    }

    public Vec3f normalize() {
        float length = 1.0f / length();
        return new Vec3f(x * length, y * length, z * length);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vec3f cross(Vec3f other) {
        return new Vec3f(
            Math.fma(y, other.z, -z * other.y),
            Math.fma(z, other.x, -x * other.z),
            Math.fma(x, other.y, -y * other.x)
        );
    }
}
