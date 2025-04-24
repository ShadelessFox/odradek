package sh.adelessfox.odradek.game.hfw.data.jolt.physics.ragdoll;

import sh.adelessfox.odradek.game.hfw.data.jolt.physics.body.BodyCreationSettings;
import sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.GroupFilter;
import sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.PhysicsMaterial;
import sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape.Shape;
import sh.adelessfox.odradek.game.hfw.data.jolt.physics.constraints.ConstraintSettings;
import sh.adelessfox.odradek.game.hfw.data.jolt.physics.constraints.TwoBodyConstraintSettings;
import sh.adelessfox.odradek.game.hfw.data.jolt.skeleton.Skeleton;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RagdollSettings {
    public static class Part {
        public BodyCreationSettings body;
        public TwoBodyConstraintSettings constraint;
    }

    public Skeleton skeleton;
    public List<Part> parts;

    public static RagdollSettings sRestoreFromBinaryState(BinaryReader reader) throws IOException {
        List<Shape> shapeMap = new ArrayList<>(1024);
        List<GroupFilter> groupFilterMap = new ArrayList<>(128);
        List<PhysicsMaterial> materialMap = new ArrayList<>(128);

        RagdollSettings settings = new RagdollSettings();
        settings.skeleton = Skeleton.restoreFromBinaryState(reader);
        settings.parts = reader.readObjects(reader.readInt(), r -> {
            Part part = new Part();
            part.body = BodyCreationSettings.sRestoreWithChildren(r, shapeMap, materialMap, groupFilterMap);

            boolean hasConstraint = r.readByteBoolean();
            if (hasConstraint) {
                part.constraint = (TwoBodyConstraintSettings) ConstraintSettings.restoreFromBinaryState(r);
            }

            return part;
        });

        return settings;
    }
}
