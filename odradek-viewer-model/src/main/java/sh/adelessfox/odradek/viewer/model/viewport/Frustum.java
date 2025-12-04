package sh.adelessfox.odradek.viewer.model.viewport;

import sh.adelessfox.odradek.math.BoundingBox;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector4f;

public final class Frustum {
    private final Vector4f[] planes = new Vector4f[6];

    public void update(Matrix4f m) {
        planes[0] = m.get(3).add(m.get(0));
        planes[1] = m.get(3).sub(m.get(0));
        planes[2] = m.get(3).add(m.get(1));
        planes[3] = m.get(3).sub(m.get(1));
        planes[4] = m.get(3).add(m.get(2));
        planes[5] = m.get(3).sub(m.get(2));
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
        // @formatter:off
        return plane.x() * (plane.x() < 0 ? minX : maxX)
             + plane.y() * (plane.y() < 0 ? minY : maxY)
             + plane.z() * (plane.z() < 0 ? minZ : maxZ) >= -plane.w();
        // @formatter:on
    }
}
