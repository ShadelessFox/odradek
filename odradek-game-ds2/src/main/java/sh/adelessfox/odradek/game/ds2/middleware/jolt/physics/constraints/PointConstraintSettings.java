package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.constraints;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class PointConstraintSettings extends TwoBodyConstraintSettings {
    public Vec3 commonPoint;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        commonPoint = JoltUtils.readVec3(reader);
    }
}
