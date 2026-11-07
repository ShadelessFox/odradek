package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.PhysicsMaterial;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Shape {
    protected int mUserData;

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
        if (shapeId == ~0) {
            return Optional.empty();
        }
        throw new NotImplementedException();
    }

    public void restoreBinaryState(BinarySource source) throws IOException {
        mUserData = source.readInt();
    }

    public void restoreMaterialState(PhysicsMaterial[] materials) {
        assert materials.length == 0;
    }

    public void restoreSubShapeState(Shape[] subShapes) {
        assert subShapes.length == 0;
    }
}
