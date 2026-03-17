package sh.adelessfox.odradek.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.middleware.jolt.math.Quat;
import sh.adelessfox.odradek.middleware.jolt.math.Vec3;

import java.io.IOException;

public class RotatedTranslatedShape extends DecoratedShape {
    public Vec3 centerOfMass;
    public Quat rotation;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        centerOfMass = JoltUtils.readAlignedVector3(reader);
        rotation = JoltUtils.readQuaternion(reader);
    }
}
