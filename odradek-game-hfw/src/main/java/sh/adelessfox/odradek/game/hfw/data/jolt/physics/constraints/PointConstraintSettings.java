package sh.adelessfox.odradek.game.hfw.data.jolt.physics.constraints;

import sh.adelessfox.odradek.game.hfw.data.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Vec3;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class PointConstraintSettings extends TwoBodyConstraintSettings {
    public Vec3 commonPoint;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        commonPoint = JoltUtils.readAlignedVector3(reader);
    }
}
