package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.math.Vec3;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class OffsetCenterOfMassShape extends DecoratedShape {
    public Vec3 offset;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        offset = JoltUtils.readAlignedVector3(source);
    }
}
