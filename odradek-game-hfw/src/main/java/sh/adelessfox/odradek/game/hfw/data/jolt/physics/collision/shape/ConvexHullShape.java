package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.data.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.data.jolt.geometry.AABox;
import sh.adelessfox.odradek.game.hfw.data.jolt.geometry.Plane;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Mat44;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Vec3;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConvexHullShape extends ConvexShape {
    public record Point(Vec3 position, int[] faces) {
        private static Point read(BinaryReader reader) throws IOException {
            final var position = JoltUtils.readAlignedVector3(reader);
            final var facesCount = reader.readInt();
            final var faces = reader.readInts(3);
            return new Point(position, Arrays.copyOf(faces, facesCount));
        }
    }

    public record Face(short firstVertex, short vertexCount) {
        private static Face read(BinaryReader reader) throws IOException {
            return new Face(reader.readShort(), reader.readShort());
        }
    }

    public Vec3 centerOfMass;
    public Mat44 inertia;
    public AABox localBounds;
    public List<Point> points;
    public List<Face> faces;
    public List<Plane> planes;
    public byte[] vertexIdx;
    public float convexRadius;
    public float volume;
    public float innerRadius;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        centerOfMass = JoltUtils.readAlignedVector3(reader);
        inertia = JoltUtils.readMatrix4(reader);
        localBounds = JoltUtils.readAABox(reader);
        points = JoltUtils.readObjects(reader, Point::read);
        faces = JoltUtils.readObjects(reader, Face::read);
        planes = JoltUtils.readObjects(reader, r -> new Plane(JoltUtils.readVec3(r), r.readFloat()));
        vertexIdx = JoltUtils.readBytes(reader);
        convexRadius = reader.readFloat();
        volume = reader.readFloat();
        innerRadius = reader.readFloat();
    }
}
