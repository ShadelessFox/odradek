package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.body;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Quat;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.CollisionGroup;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.GroupFilter;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.PhysicsMaterial;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.shape.Shape;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

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
        BinarySource source,
        List<Shape> shapeMap,
        List<PhysicsMaterial> materialMap,
        List<GroupFilter> groupFilterMap
    ) throws IOException {
        BodyCreationSettings settings = new BodyCreationSettings();
        settings.restoreBinaryState(source);
        settings.shape = Shape.sRestoreWithChildren(source, shapeMap, materialMap).orElse(null);

        int groupFilterId = source.readInt();
        if (groupFilterId != -1) {
            GroupFilter groupFilter;

            if (groupFilterId >= groupFilterMap.size()) {
                assert groupFilterId == groupFilterMap.size();
                groupFilter = GroupFilter.sRestoreFromBinaryState(source);
                groupFilterMap.add(groupFilter);
            } else {
                groupFilter = groupFilterMap.get(groupFilterId);
            }

            settings.collisionGroup.groupFilter = groupFilter;
        }

        return settings;
    }

    public void restoreBinaryState(BinarySource source) throws IOException {
        position = JoltUtils.readVec3(source);
        rotation =  JoltUtils.readQuaternion(source);
        linearVelocity = JoltUtils.readVec3(source);
        angularVelocity = JoltUtils.readVec3(source);
        collisionGroup = CollisionGroup.restoreFromBinaryState(source);
        objectLayer = source.readShort();
        motionType = MotionType.values()[source.readByte()];
        allowDynamicOrKinematic = source.readBool(BoolFormat.BYTE);
        motionQuality = MotionQuality.values()[source.readByte()];
        allowSleeping = source.readBool(BoolFormat.BYTE);
        friction = source.readFloat();
        restitution = source.readFloat();
        linearDamping = source.readFloat();
        angularDamping = source.readFloat();
        maxLinearVelocity = source.readFloat();
        maxAngularVelocity = source.readFloat();
        gravityFactor = source.readFloat();
        overrideMassProperties = OverrideMassProperties.values()[source.readByte()];
        inertiaMultiplier = source.readFloat();
        massPropertiesOverride = MassProperties.restoreFromBinaryState(source);
    }
}
