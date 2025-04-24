package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.data.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.data.jolt.geometry.AABox;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Vec3;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;

public class CompoundShape extends Shape {
    public static class SubShape {
        public final int userData;
        public final Vec3 position;
        public final Vec3 rotation;
        public Shape shape;

        public SubShape(int userData, Vec3 position, Vec3 rotation) {
            this.userData = userData;
            this.position = position;
            this.rotation = rotation;
        }

        private static SubShape read(BinaryReader reader) throws IOException {
            var userData = reader.readInt();
            var position = JoltUtils.readVec3(reader);
            var rotation = JoltUtils.readVec3(reader);

            return new SubShape(userData, position, rotation);
        }
    }

    public Vec3 centerOfMass;
    public AABox localBounds;
    public float innerRadius;
    public List<SubShape> subShapes;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);
        centerOfMass = JoltUtils.readAlignedVector3(reader);
        localBounds = JoltUtils.readAABox(reader);
        innerRadius = reader.readFloat();
        subShapes = JoltUtils.readObjects(reader, SubShape::read);
    }

    @Override
    public void restoreSubShapeState(Shape[] subShapes) {
        assert this.subShapes.size() == subShapes.length;
        for (int i = 0; i < subShapes.length; i++) {
            this.subShapes.get(i).shape = subShapes[i];
        }
    }
}
