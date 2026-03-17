package sh.adelessfox.odradek.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.middleware.jolt.math.Vec3;

import java.io.IOException;

public class BoxShape extends ConvexShape {
    public Vec3 halfExtent;
    public float convexRadius;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        halfExtent = JoltUtils.readAlignedVector3(reader);
        convexRadius = reader.readFloat();
    }
}
