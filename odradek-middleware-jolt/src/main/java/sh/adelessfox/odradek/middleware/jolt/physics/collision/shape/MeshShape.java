package sh.adelessfox.odradek.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.middleware.jolt.physics.collision.PhysicsMaterial;

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
