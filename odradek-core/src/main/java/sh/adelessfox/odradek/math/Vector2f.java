package sh.adelessfox.odradek.math;

/**
 * Contains the definition of an immutable vector comprising 2 floats and associated transformations.
 *
 * @param x the x component of the vector
 * @param y the y component of the vector
 */
public record Vector2f(float x, float y) {
    public static final int BYTES = Float.BYTES * 2;

    public Vector2f mul(float xy) {
        return mul(xy, xy);
    }

    public Vector2f mul(float x, float y) {
        return new Vector2f(this.x * x, this.y * y);
    }
}
