package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class TaperedCapsuleShape extends ConvexShape {
    public Vec3 centerOfMass;
    public float topRadius;
    public float bottomRadius;
    public float topCenter;
    public float bottomCenter;
    public float convexRadius;
    public float sinAlpha;
    public float tanAlpha;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        centerOfMass = JoltUtils.readVec3(source);
        topRadius = source.readFloat();
        bottomRadius = source.readFloat();
        topCenter = source.readFloat();
        bottomCenter = source.readFloat();
        convexRadius = source.readFloat();
        sinAlpha = source.readFloat();
        tanAlpha = source.readFloat();
    }
}
