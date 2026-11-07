package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.geometry.AABox;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.geometry.Plane;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.math.Mat44;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.math.Vec3;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.collect.Ints;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.List;

public class ConvexHullShape extends ConvexShape {
    public record Point(Vec3 position, Ints faces) {
        private static Point read(BinarySource source) throws IOException {
            final var position = JoltUtils.readAlignedVector3(source);
            final var facesCount = source.readInt();
            final var faces = source.readInts(3);
            return new Point(position, faces.asBytes().asInts());
        }
    }

    public record Face(short firstVertex, short vertexCount) {
        private static Face read(BinarySource source) throws IOException {
            return new Face(source.readShort(), source.readShort());
        }
    }

    public Vec3 centerOfMass;
    public Mat44 inertia;
    public AABox localBounds;
    public List<Point> points;
    public List<Face> faces;
    public List<Plane> planes;
    public Bytes vertexIdx;
    public float convexRadius;
    public float volume;
    public float innerRadius;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        centerOfMass = JoltUtils.readAlignedVector3(source);
        inertia = JoltUtils.readMatrix4(source);
        localBounds = JoltUtils.readAABox(source);
        points = JoltUtils.readObjects(source, Point::read);
        faces = JoltUtils.readObjects(source, Face::read);
        planes = JoltUtils.readObjects(source, r -> new Plane(JoltUtils.readVec3(r), r.readFloat()));
        vertexIdx = JoltUtils.readBytes(source);
        convexRadius = source.readFloat();
        volume = source.readFloat();
        innerRadius = source.readFloat();
    }
}
