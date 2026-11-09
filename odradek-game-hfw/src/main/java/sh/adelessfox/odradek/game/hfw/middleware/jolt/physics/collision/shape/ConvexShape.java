package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.PhysicsMaterial;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class ConvexShape extends Shape {
    public float density;
    public PhysicsMaterial material;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        density = source.readFloat();
    }

    @Override
    public void restoreMaterialState(PhysicsMaterial[] materials) {
        assert materials.length == 1;
        material = materials[0];
    }
}
