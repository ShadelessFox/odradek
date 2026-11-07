package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.shape;

import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class SphereShape extends ConvexShape {
    public float radius;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        radius = source.readFloat();
    }
}
