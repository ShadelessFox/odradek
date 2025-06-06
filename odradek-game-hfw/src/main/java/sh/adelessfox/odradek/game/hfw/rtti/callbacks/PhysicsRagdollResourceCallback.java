package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.data.jolt.physics.ragdoll.RagdollSettings;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class PhysicsRagdollResourceCallback implements ExtraBinaryDataCallback<PhysicsRagdollResourceCallback.PhysicsRagdollData> {
    public interface PhysicsRagdollData {
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, PhysicsRagdollData object) throws IOException {
        // FIXME: Skipped for now
        var ragdoll = RagdollSettings.sRestoreFromBinaryState(reader);
    }
}
