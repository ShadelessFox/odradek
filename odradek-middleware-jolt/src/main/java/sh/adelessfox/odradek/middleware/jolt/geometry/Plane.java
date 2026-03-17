package sh.adelessfox.odradek.middleware.jolt.geometry;

import sh.adelessfox.odradek.middleware.jolt.math.Vec3;

public record Plane(Vec3 normal, float distance) {
}
