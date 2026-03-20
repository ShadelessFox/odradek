package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class OffsetCenterOfMassShape extends DecoratedShape {
    public Vec3 offset;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        offset = JoltUtils.readVec3(reader);
    }
}
