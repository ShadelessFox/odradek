package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class ScaledShape extends DecoratedShape {
    public Vec3 scale;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        scale = JoltUtils.readVec3(source);
    }
}
