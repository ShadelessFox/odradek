package sh.adelessfox.odradek.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.middleware.jolt.math.Vec3;

import java.io.IOException;

public class ScaledShape extends DecoratedShape {
    public Vec3 scale;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        scale = JoltUtils.readAlignedVector3(reader);
    }
}
