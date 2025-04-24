package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class CylinderShape extends ConvexShape {
    public float halfHeight;
    public float radius;
    public float convexRadius;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        halfHeight = reader.readFloat();
        radius = reader.readFloat();
        convexRadius = reader.readFloat();
    }
}
