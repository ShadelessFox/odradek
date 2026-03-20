package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.PhysicsMaterial;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Shape {
    protected long mUserData;

    public static Shape sRestoreFromBinaryState(BinaryReader reader) throws IOException {
        var shapeSubType = ShapeSubType.values()[reader.readByte()];
        var shape = ShapeFunctions.get(shapeSubType).construct();
        shape.restoreBinaryState(reader);

        return shape;
    }

    public static Optional<Shape> sRestoreWithChildren(
        BinaryReader reader,
        List<Shape> shapeMap,
        List<PhysicsMaterial> materialMap
    ) throws IOException {
        var shapeId = reader.readInt();

        // Check nullptr shape
        if (shapeId == -1) {
            return Optional.empty();
        }

        // Check if we already read this shape
        if (shapeId < shapeMap.size()) {
            return Optional.of(shapeMap.get(shapeId));
        }

        // Read the shape
        var shape = sRestoreFromBinaryState(reader);
        shapeMap.add(shape);

        // Read the sub shapes
        var subShapes = reader.readObjects(reader.readInt(), r -> sRestoreWithChildren(r, shapeMap, materialMap).orElse(null));
        shape.restoreSubShapeState(subShapes.toArray(Shape[]::new));

        throw new NotImplementedException();
    }

    public void restoreBinaryState(BinaryReader reader) throws IOException {
        mUserData = reader.readLong();
    }

    public void restoreMaterialState(PhysicsMaterial[] materials) {
        assert materials.length == 0;
    }

    public void restoreSubShapeState(Shape[] subShapes) {
        assert subShapes.length == 0;
    }
}
