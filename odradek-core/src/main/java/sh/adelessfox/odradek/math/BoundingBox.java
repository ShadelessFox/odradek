package sh.adelessfox.odradek.math;

public record BoundingBox(
    Vector3f min,
    Vector3f max
) {
    private static final BoundingBox empty = new BoundingBox(Vector3f.zero(), Vector3f.zero());

    public static BoundingBox empty() {
        return empty;
    }

    public Vector3f center() {
        return min.add(max).mul(0.5f);
    }

    public BoundingBox transform(Matrix4f matrix) {
        var corners = new Vector3f[]{
            new Vector3f(min.x(), min.y(), min.z()),
            new Vector3f(min.x(), min.y(), max.z()),
            new Vector3f(min.x(), max.y(), min.z()),
            new Vector3f(min.x(), max.y(), max.z()),
            new Vector3f(max.x(), min.y(), min.z()),
            new Vector3f(max.x(), min.y(), max.z()),
            new Vector3f(max.x(), max.y(), min.z()),
            new Vector3f(max.x(), max.y(), max.z())
        };

        var min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        var max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        for (var corner : corners) {
            var transformedCorner = corner.transform(matrix);
            min = min.min(transformedCorner);
            max = max.max(transformedCorner);
        }

        return new BoundingBox(min, max);
    }

    public BoundingBox encapsulate(BoundingBox other) {
        var min = min().min(other.min());
        var max = max().max(other.max());
        return new BoundingBox(min, max);
    }

    public BoundingBox encapsulate(float x, float y, float z) {
        var min = min().min(x, y, z);
        var max = max().max(x, y, z);
        return new BoundingBox(min, max);
    }
}
