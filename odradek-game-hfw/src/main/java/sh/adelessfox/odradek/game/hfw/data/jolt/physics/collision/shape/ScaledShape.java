package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.data.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Vec3;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class ScaledShape extends DecoratedShape {
    public Vec3 scale;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        scale = JoltUtils.readAlignedVector3(reader);
    }
}
