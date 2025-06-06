package sh.adelessfox.odradek.game.hfw.data.jolt.physics.constraints;

import sh.adelessfox.odradek.game.hfw.data.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Vec3;
import sh.adelessfox.odradek.io.BinaryReader;

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
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        point1 = JoltUtils.readAlignedVector3(reader);
        hingeAxis1 = JoltUtils.readAlignedVector3(reader);
        normalAxis1 = JoltUtils.readAlignedVector3(reader);
        point2 = JoltUtils.readAlignedVector3(reader);
        hingeAxis2 = JoltUtils.readAlignedVector3(reader);
        normalAxis2 = JoltUtils.readAlignedVector3(reader);
        limitsMin = reader.readFloat();
        limitsMax = reader.readFloat();
        maxFrictionForce = reader.readFloat();
        motorSettings.restoreBinaryState(reader);
    }
}
