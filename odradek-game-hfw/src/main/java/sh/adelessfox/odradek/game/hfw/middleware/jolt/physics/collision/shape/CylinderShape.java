package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.shape;

import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class CylinderShape extends ConvexShape {
    public float halfHeight;
    public float radius;
    public float convexRadius;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        halfHeight = source.readFloat();
        radius = source.readFloat();
        convexRadius = source.readFloat();
    }
}
