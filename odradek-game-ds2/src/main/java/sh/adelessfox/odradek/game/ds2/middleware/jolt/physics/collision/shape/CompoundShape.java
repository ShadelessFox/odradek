package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.geometry.AABox;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import wtf.reversed.toolbox.io.BinarySource;

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

        private static SubShape read(BinarySource source) throws IOException {
            var userData = source.readInt();
            var position = JoltUtils.readVec3(source);
            var rotation = JoltUtils.readVec3(source);

            return new SubShape(userData, position, rotation);
        }
    }

    public Vec3 centerOfMass;
    public AABox localBounds;
    public float innerRadius;
    public List<SubShape> subShapes;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);
        centerOfMass = JoltUtils.readVec3(source);
        localBounds = JoltUtils.readAABox(source);
        innerRadius = source.readFloat();
        subShapes = JoltUtils.readObjects(source, SubShape::read);
    }

    @Override
    public void restoreSubShapeState(Shape[] subShapes) {
        assert this.subShapes.size() == subShapes.length;
        for (int i = 0; i < subShapes.length; i++) {
            this.subShapes.get(i).shape = subShapes[i];
        }
    }
}
