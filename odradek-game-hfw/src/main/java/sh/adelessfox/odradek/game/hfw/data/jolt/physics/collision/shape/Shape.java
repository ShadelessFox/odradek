package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.PhysicsMaterial;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Shape {
    protected int mUserData;

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
        if (shapeId == ~0) {
            return Optional.empty();
        }
        throw new NotImplementedException();
    }

    public void restoreBinaryState(BinaryReader reader) throws IOException {
        mUserData = reader.readInt();
    }

    public void restoreMaterialState(PhysicsMaterial[] materials) {
        assert materials.length == 0;
    }

    public void restoreSubShapeState(Shape[] subShapes) {
        assert subShapes.length == 0;
    }
}
