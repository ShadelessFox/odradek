package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.body;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Quat;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.CollisionGroup;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.GroupFilter;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.PhysicsMaterial;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.shape.Shape;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;

public class BodyCreationSettings {
    public enum OverrideMassProperties {
        CalculateMassAndInertia,
        CalculateInertia,
        MassAndInertiaProvided
    }

    public Vec3 position;
    public Quat rotation;
    public Vec3 linearVelocity;
    public Vec3 angularVelocity;
    public CollisionGroup collisionGroup;
    public short objectLayer;
    public MotionType motionType;
    public boolean allowDynamicOrKinematic;
    public MotionQuality motionQuality;
    public boolean allowSleeping;
    public float friction;
    public float restitution;
    public float linearDamping;
    public float angularDamping;
    public float maxLinearVelocity;
    public float maxAngularVelocity;
    public float gravityFactor;
    public OverrideMassProperties overrideMassProperties;
    public float inertiaMultiplier;
    public MassProperties massPropertiesOverride;
    public Shape shape;

    public static BodyCreationSettings sRestoreWithChildren(
        BinaryReader reader,
        List<Shape> shapeMap,
        List<PhysicsMaterial> materialMap,
        List<GroupFilter> groupFilterMap
    ) throws IOException {
        BodyCreationSettings settings = new BodyCreationSettings();
        settings.restoreBinaryState(reader);
        settings.shape = Shape.sRestoreWithChildren(reader, shapeMap, materialMap).orElse(null);

        int groupFilterId = reader.readInt();
        if (groupFilterId != -1) {
            GroupFilter groupFilter;

            if (groupFilterId >= groupFilterMap.size()) {
                assert groupFilterId == groupFilterMap.size();
                groupFilter = GroupFilter.sRestoreFromBinaryState(reader);
                groupFilterMap.add(groupFilter);
            } else {
                groupFilter = groupFilterMap.get(groupFilterId);
            }

            settings.collisionGroup.groupFilter = groupFilter;
        }

        return settings;
    }

    public void restoreBinaryState(BinaryReader reader) throws IOException {
        position = JoltUtils.readVec3(reader);
        rotation =  JoltUtils.readQuaternion(reader);
        linearVelocity = JoltUtils.readVec3(reader);
        angularVelocity = JoltUtils.readVec3(reader);
        collisionGroup = CollisionGroup.restoreFromBinaryState(reader);
        objectLayer = reader.readShort();
        motionType = MotionType.values()[reader.readByte()];
        allowDynamicOrKinematic = reader.readByteBoolean();
        motionQuality = MotionQuality.values()[reader.readByte()];
        allowSleeping = reader.readByteBoolean();
        friction = reader.readFloat();
        restitution = reader.readFloat();
        linearDamping = reader.readFloat();
        angularDamping = reader.readFloat();
        maxLinearVelocity = reader.readFloat();
        maxAngularVelocity = reader.readFloat();
        gravityFactor = reader.readFloat();
        overrideMassProperties = OverrideMassProperties.values()[reader.readByte()];
        inertiaMultiplier = reader.readFloat();
        massPropertiesOverride = MassProperties.restoreFromBinaryState(reader);
    }
}
