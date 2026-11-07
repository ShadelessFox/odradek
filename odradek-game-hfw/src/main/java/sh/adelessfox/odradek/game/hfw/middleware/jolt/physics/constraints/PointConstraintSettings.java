package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.constraints;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.math.Vec3;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class PointConstraintSettings extends TwoBodyConstraintSettings {
    public Vec3 commonPoint;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        commonPoint = JoltUtils.readAlignedVector3(source);
    }
}
