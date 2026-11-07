package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.constraints;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class SwingTwistConstraintSettings extends TwoBodyConstraintSettings {
    public Vec3 position1;
    public Vec3 twistAxis1;
    public Vec3 planeAxis1;
    public Vec3 position2;
    public Vec3 twistAxis2;
    public Vec3 planeAxis2;
    public float normalHalfConeAngle;
    public float planeHalfConeAngle;
    public float twistMinAngle;
    public float twistMaxAngle;
    public float maxFrictionTorque;
    public final MotorSettings swingMotorSettings = new MotorSettings();
    public final MotorSettings twistMotorSettings = new MotorSettings();

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        position1 = JoltUtils.readVec3(source);
        twistAxis1 = JoltUtils.readVec3(source);
        planeAxis1 = JoltUtils.readVec3(source);
        position2 = JoltUtils.readVec3(source);
        twistAxis2 = JoltUtils.readVec3(source);
        planeAxis2 = JoltUtils.readVec3(source);
        normalHalfConeAngle = source.readFloat();
        planeHalfConeAngle = source.readFloat();
        twistMinAngle = source.readFloat();
        twistMaxAngle = source.readFloat();
        maxFrictionTorque = source.readFloat();
        swingMotorSettings.restoreBinaryState(source);
        twistMotorSettings.restoreBinaryState(source);
    }
}
