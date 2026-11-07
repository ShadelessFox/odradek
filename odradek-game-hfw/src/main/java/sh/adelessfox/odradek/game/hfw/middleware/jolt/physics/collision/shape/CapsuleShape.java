package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.shape;

import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class CapsuleShape extends ConvexShape {
    public float radius;
    public float halfHeightOfCylinder;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        radius = source.readFloat();
        halfHeightOfCylinder = source.readFloat();
    }
}
