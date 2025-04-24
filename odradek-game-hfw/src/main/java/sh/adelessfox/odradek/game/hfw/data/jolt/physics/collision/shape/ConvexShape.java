package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.PhysicsMaterial;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class ConvexShape extends Shape {
    public float density;
    public PhysicsMaterial material;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        density = reader.readFloat();
    }

    @Override
    public void restoreMaterialState(PhysicsMaterial[] materials) {
        assert materials.length == 1;
        material = materials[0];
    }
}
