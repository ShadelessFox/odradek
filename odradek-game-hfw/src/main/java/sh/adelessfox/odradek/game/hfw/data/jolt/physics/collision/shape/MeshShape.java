package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.data.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.PhysicsMaterial;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class MeshShape extends Shape {
    public byte[] tree;
    public PhysicsMaterial[] materials;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        tree = JoltUtils.readBytes(reader);
    }

    @Override
    public void restoreMaterialState(PhysicsMaterial[] materials) {
        this.materials = materials;
    }
}
