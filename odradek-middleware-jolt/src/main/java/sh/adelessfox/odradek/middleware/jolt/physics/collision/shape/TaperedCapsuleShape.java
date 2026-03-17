package sh.adelessfox.odradek.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.middleware.jolt.math.Vec3;

import java.io.IOException;

public class TaperedCapsuleShape extends ConvexShape {
    public Vec3 centerOfMass;
    public float topRadius;
    public float bottomRadius;
    public float topCenter;
    public float bottomCenter;
    public float convexRadius;
    public float sinAlpha;
    public float tanAlpha;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        centerOfMass = JoltUtils.readAlignedVector3(reader);
        topRadius = reader.readFloat();
        bottomRadius = reader.readFloat();
        topCenter = reader.readFloat();
        bottomCenter = reader.readFloat();
        convexRadius = reader.readFloat();
        sinAlpha = reader.readFloat();
        tanAlpha = reader.readFloat();
    }
}
