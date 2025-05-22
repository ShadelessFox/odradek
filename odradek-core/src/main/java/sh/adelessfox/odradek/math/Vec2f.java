package sh.adelessfox.odradek.math;

public record Vec2f(float x, float y) {
    public Vec2f mul(float scalar) {
        return mul(scalar, scalar);
    }

    public Vec2f mul(float x, float y) {
        return new Vec2f(this.x * x, this.y * y);
    }
}
