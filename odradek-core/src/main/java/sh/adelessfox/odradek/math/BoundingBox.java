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
        return new Vector3f(
            (min.x() + max.x()) / 2,
            (min.y() + max.y()) / 2,
            (min.z() + max.z()) / 2
        );
    }

    public BoundingBox transform(Matrix4f matrix) {
        var corners = new Vector3f[] {
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
            var transformedCorner = matrix.transform(corner);
            min = new Vector3f(
                Math.min(min.x(), transformedCorner.x()),
                Math.min(min.y(), transformedCorner.y()),
                Math.min(min.z(), transformedCorner.z())
            );
            max = new Vector3f(
                Math.max(max.x(), transformedCorner.x()),
                Math.max(max.y(), transformedCorner.y()),
                Math.max(max.z(), transformedCorner.z())
            );
        }

        return new BoundingBox(min, max);
    }

    public BoundingBox union(BoundingBox other) {
        var min = new Vector3f(
            Math.min(this.min.x(), other.min.x()),
            Math.min(this.min.y(), other.min.y()),
            Math.min(this.min.z(), other.min.z())
        );
        var max = new Vector3f(
            Math.max(this.max.x(), other.max.x()),
            Math.max(this.max.y(), other.max.y()),
            Math.max(this.max.z(), other.max.z())
        );
        return new BoundingBox(min, max);
    }

    public BoundingBox union(float x, float y, float z) {
        var min = new Vector3f(
            Math.min(this.min.x(), x),
            Math.min(this.min.y(), y),
            Math.min(this.min.z(), z)
        );
        var max = new Vector3f(
            Math.max(this.max.x(), x),
            Math.max(this.max.y(), y),
            Math.max(this.max.z(), z)
        );
        return new BoundingBox(min, max);
    }
}
