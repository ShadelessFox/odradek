package sh.adelessfox.odradek.middleware.jolt.physics.constraints;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.middleware.jolt.math.Vec3;

import java.io.IOException;

public class SliderConstraintSettings extends TwoBodyConstraintSettings {
    public Vec3 sliderAxis;
    public float limitsMin;
    public float limitsMax;
    public float maxFrictionForce;
    public final MotorSettings swingMotorSettings = new MotorSettings();

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        sliderAxis = JoltUtils.readAlignedVector3(reader);
        limitsMin = reader.readFloat();
        limitsMax = reader.readFloat();
        maxFrictionForce = reader.readFloat();
        swingMotorSettings.restoreBinaryState(reader);
    }
}
