package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.PhysicsMaterial;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class MeshShape extends Shape {
    public Bytes tree;
    public PhysicsMaterial[] materials;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        tree = JoltUtils.readBytes(source);
    }

    @Override
    public void restoreMaterialState(PhysicsMaterial[] materials) {
        this.materials = materials;
    }
}
