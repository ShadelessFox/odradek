package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import java.util.Map;
import java.util.function.Supplier;

public final class ShapeFunctions {
    private static final Map<ShapeSubType, ShapeFunctions> factory = Map.ofEntries(
        Map.entry(ShapeSubType.Box, new ShapeFunctions(BoxShape::new)),
        Map.entry(ShapeSubType.Capsule, new ShapeFunctions(CapsuleShape::new)),
        Map.entry(ShapeSubType.ConvexHull, new ShapeFunctions(ConvexHullShape::new)),
        Map.entry(ShapeSubType.Cylinder, new ShapeFunctions(CylinderShape::new)),
        Map.entry(ShapeSubType.HeightField, new ShapeFunctions(HeightFieldShape::new)),
        Map.entry(ShapeSubType.Mesh, new ShapeFunctions(MeshShape::new)),
        Map.entry(ShapeSubType.OffsetCenterOfMass, new ShapeFunctions(OffsetCenterOfMassShape::new)),
        Map.entry(ShapeSubType.RotatedTranslated, new ShapeFunctions(RotatedTranslatedShape::new)),
        Map.entry(ShapeSubType.Scaled, new ShapeFunctions(ScaledShape::new)),
        Map.entry(ShapeSubType.Sphere, new ShapeFunctions(SphereShape::new)),
        Map.entry(ShapeSubType.StaticCompound, new ShapeFunctions(StaticCompoundShape::new)),
        Map.entry(ShapeSubType.TaperedCapsule, new ShapeFunctions(TaperedCapsuleShape::new))
    );

    private final Supplier<Shape> constructor;

    private ShapeFunctions(Supplier<Shape> constructor) {
        this.constructor = constructor;
    }

    public static ShapeFunctions get(ShapeSubType shapeSubType) {
        var functions = factory.get(shapeSubType);
        if (functions == null) {
            throw new IllegalArgumentException("Unknown shape subtype: " + shapeSubType);
        }
        return functions;
    }

    public Shape construct() {
        return constructor.get();
    }
}
