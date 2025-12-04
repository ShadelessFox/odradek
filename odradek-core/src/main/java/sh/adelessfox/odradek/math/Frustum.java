package sh.adelessfox.odradek.math;

import java.util.List;

public record Frustum(List<Vector4f> planes) {
    public static Frustum of(Matrix4f m) {
        var planes = List.of(
            m.get(3).add(m.get(0)),
            m.get(3).sub(m.get(0)),
            m.get(3).add(m.get(1)),
            m.get(3).sub(m.get(1)),
            m.get(3).add(m.get(2)),
            m.get(3).sub(m.get(2))
        );
        return new Frustum(planes);
    }

    public boolean test(BoundingBox box) {
        return test(box.min().x(), box.min().y(), box.min().z(), box.max().x(), box.max().y(), box.max().z());
    }

    private boolean test(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        for (Vector4f p : planes) {
            if (!isOnForwardPlane(p, minX, minY, minZ, maxX, maxY, maxZ)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isOnForwardPlane(
        Vector4f plane,
        float minX, float minY, float minZ,
        float maxX, float maxY, float maxZ
    ) {
        return plane.x() * (plane.x() < 0 ? minX : maxX)
            + plane.y() * (plane.y() < 0 ? minY : maxY)
            + plane.z() * (plane.z() < 0 ? minZ : maxZ) >= -plane.w();
    }
}
