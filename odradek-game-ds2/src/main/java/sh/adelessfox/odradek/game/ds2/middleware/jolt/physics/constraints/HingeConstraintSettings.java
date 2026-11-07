package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.constraints;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class HingeConstraintSettings extends TwoBodyConstraintSettings {
    public Vec3 point1;
    public Vec3 hingeAxis1;
    public Vec3 normalAxis1;
    public Vec3 point2;
    public Vec3 hingeAxis2;
    public Vec3 normalAxis2;
    public float limitsMin;
    public float limitsMax;
    public float maxFrictionForce;
    public final MotorSettings motorSettings = new MotorSettings();

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        point1 = JoltUtils.readVec3(source);
        hingeAxis1 = JoltUtils.readVec3(source);
        normalAxis1 = JoltUtils.readVec3(source);
        point2 = JoltUtils.readVec3(source);
        hingeAxis2 = JoltUtils.readVec3(source);
        normalAxis2 = JoltUtils.readVec3(source);
        limitsMin = source.readFloat();
        limitsMax = source.readFloat();
        maxFrictionForce = source.readFloat();
        motorSettings.restoreBinaryState(source);
    }
}
