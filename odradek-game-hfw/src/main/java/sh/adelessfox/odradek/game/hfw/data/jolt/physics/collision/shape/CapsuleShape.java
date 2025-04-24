package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class CapsuleShape extends ConvexShape {
    public float radius;
    public float halfHeightOfCylinder;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        radius = reader.readFloat();
        halfHeightOfCylinder = reader.readFloat();
    }
}
