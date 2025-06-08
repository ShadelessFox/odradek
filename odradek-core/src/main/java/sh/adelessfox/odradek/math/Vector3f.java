package sh.adelessfox.odradek.math;

/**
 * Contains the definition of an immutable vector comprising 3 floats and associated transformations.
 *
 * @param x the x component of the vector
 * @param y the y component of the vector
 * @param z the z component of the vector
 */
public record Vector3f(float x, float y, float z) {
    public static final int BYTES = Float.BYTES * 3;

    public Vector3f add(Vector3f other) {
        return add(other.x, other.y, other.z);
    }

    public Vector3f add(float x, float y, float z) {
        return new Vector3f(this.x + x, this.y + y, this.z + z);
    }

    public Vector3f sub(Vector3f other) {
        return sub(other.x, other.y, other.z);
    }

    public Vector3f sub(float x, float y, float z) {
        return new Vector3f(this.x - x, this.y - y, this.z - z);
    }

    public Vector3f mul(Vector3f other) {
        return mul(other.x, other.y, other.z);
    }

    public Vector3f mul(float x, float y, float z) {
        return new Vector3f(this.x * x, this.y * y, this.z * z);
    }

    public Vector3f mul(float xyz) {
        return mul(xyz, xyz, xyz);
    }

    public float distance(Vector3f other) {
        return distance(other.x, other.y, other.z);
    }

    public float distance(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return (float) Math.sqrt(Math.fma(dx, dx, Math.fma(dy, dy, dz * dz)));
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3f normalize() {
        float length = 1.0f / length();
        return new Vector3f(x * length, y * length, z * length);
    }

    public Vector3f cross(Vector3f other) {
        return new Vector3f(
            Math.fma(y, other.z, -z * other.y),
            Math.fma(z, other.x, -x * other.z),
            Math.fma(x, other.y, -y * other.x)
        );
    }
}
