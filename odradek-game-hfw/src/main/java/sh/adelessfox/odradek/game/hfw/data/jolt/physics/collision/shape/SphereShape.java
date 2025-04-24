package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class SphereShape extends ConvexShape {
    public float radius;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        radius = reader.readFloat();
    }
}
