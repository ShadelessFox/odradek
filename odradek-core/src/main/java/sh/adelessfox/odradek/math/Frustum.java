package sh.adelessfox.odradek.math;

import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.math.Matrix4;
import wtf.reversed.toolbox.math.Vector4;

import java.util.List;

public record Frustum(List<Vector4> planes) {
    public Frustum {
        if (planes.size() != 6) {
            throw new IllegalArgumentException("Frustum must have 6 planes, got %d".formatted(planes.size()));
        }
        planes = List.copyOf(planes);
    }

    public static Frustum of(Matrix4 m) {
        var m1 = new Vector4(m.m11(), m.m12(), m.m13(), m.m14());
        var m2 = new Vector4(m.m21(), m.m22(), m.m23(), m.m24());
        var m3 = new Vector4(m.m31(), m.m32(), m.m33(), m.m34());
        var m4 = new Vector4(m.m41(), m.m42(), m.m43(), m.m44());

        var planes = List.of(
            m4.add(m1), m4.subtract(m1),
            m4.add(m2), m4.subtract(m2),
            m4.add(m3), m4.subtract(m3)
        );

        return new Frustum(planes);
    }

    public boolean test(Bounds bounds) {
        return test(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ());
    }

    private boolean test(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        for (Vector4 p : planes) {
            if (!isOnForwardPlane(p, minX, minY, minZ, maxX, maxY, maxZ)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isOnForwardPlane(
        Vector4 plane,
        float minX, float minY, float minZ,
        float maxX, float maxY, float maxZ
    ) {
        return plane.x() * (plane.x() < 0 ? minX : maxX)
            + plane.y() * (plane.y() < 0 ? minY : maxY)
            + plane.z() * (plane.z() < 0 ? minZ : maxZ) >= -plane.w();
    }
}
