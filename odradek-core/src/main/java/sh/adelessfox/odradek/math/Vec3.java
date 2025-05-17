package sh.adelessfox.odradek.math;

public record Vec3(float x, float y, float z) {
    private static final Vec3 one = new Vec3(1, 1, 1);

    public static Vec3 one() {
        return one;
    }

    public Vec3 add(Vec3 other) {
        return add(other.x, other.y, other.z);
    }

    public Vec3 add(float x, float y, float z) {
        return new Vec3(this.x + x, this.y + y, this.z + z);
    }

    public Vec3 sub(Vec3 other) {
        return sub(other.x, other.y, other.z);
    }

    public Vec3 sub(float x, float y, float z) {
        return new Vec3(this.x - x, this.y - y, this.z - z);
    }

    public Vec3 mul(float scalar) {
        return mul(scalar, scalar, scalar);
    }

    public Vec3 mul(Vec3 other) {
        return mul(other.x, other.y, other.z);
    }

    public Vec3 mul(float x, float y, float z) {
        return new Vec3(this.x * x, this.y * y, this.z * z);
    }

    public float distance(Vec3 other) {
        return distance(other.x, other.y, other.z);
    }

    public float distance(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return (float) Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, dz * dz)));
    }

    public Vec3 normalize() {
        float length = 1.0f / length();
        return new Vec3(x * length, y * length, z * length);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vec3 cross(Vec3 other) {
        return new Vec3(
            Math.fma(y, other.z, -z * other.y),
            Math.fma(z, other.x, -x * other.z),
            Math.fma(x, other.y, -y * other.x)
        );
    }
}
