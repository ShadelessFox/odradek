package sh.adelessfox.odradek.middleware.jolt.physics.constraints;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.middleware.jolt.math.Vec3;

import java.io.IOException;

public class PointConstraintSettings extends TwoBodyConstraintSettings {
    public Vec3 commonPoint;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        commonPoint = JoltUtils.readAlignedVector3(reader);
    }
}
