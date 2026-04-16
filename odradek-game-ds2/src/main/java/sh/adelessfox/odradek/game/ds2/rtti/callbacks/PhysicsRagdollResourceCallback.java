package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.ragdoll.RagdollSettings;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public final class PhysicsRagdollResourceCallback implements ExtraBinaryDataCallback<DS2.PhysicsRagdollResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DS2.PhysicsRagdollResource object) throws IOException {
        // NOTE: Deserialized and thrown away; to serialize back, must be stored somewhere
        RagdollSettings.sRestoreFromBinaryState(reader);
    }
}
