package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.PhysicsMaterial;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Shape {
    protected long mUserData;

    public static Shape sRestoreFromBinaryState(BinarySource source) throws IOException {
        var shapeSubType = ShapeSubType.values()[source.readByte()];
        var shape = ShapeFunctions.get(shapeSubType).construct();
        shape.restoreBinaryState(source);

        return shape;
    }

    public static Optional<Shape> sRestoreWithChildren(
        BinarySource source,
        List<Shape> shapeMap,
        List<PhysicsMaterial> materialMap
    ) throws IOException {
        var shapeId = source.readInt();

        // Check nullptr shape
        if (shapeId == -1) {
            return Optional.empty();
        }

        // Check if we already read this shape
        if (shapeId < shapeMap.size()) {
            return Optional.of(shapeMap.get(shapeId));
        }

        // Read the shape
        var shape = sRestoreFromBinaryState(source);
        shapeMap.add(shape);

        // Read the sub shapes
        var subShapes = source.readObjects(source.readInt(), r -> sRestoreWithChildren(r, shapeMap, materialMap).orElse(null));
        shape.restoreSubShapeState(subShapes.toArray(Shape[]::new));

        throw new NotImplementedException();
    }

    public void restoreBinaryState(BinarySource source) throws IOException {
        mUserData = source.readLong();
    }

    public void restoreMaterialState(PhysicsMaterial[] materials) {
        assert materials.length == 0;
    }

    public void restoreSubShapeState(Shape[] subShapes) {
        assert subShapes.length == 0;
    }
}
