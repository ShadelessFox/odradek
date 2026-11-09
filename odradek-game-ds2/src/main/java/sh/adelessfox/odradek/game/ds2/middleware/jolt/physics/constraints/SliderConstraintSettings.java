package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.constraints;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class SliderConstraintSettings extends TwoBodyConstraintSettings {
    public Vec3 sliderAxis;
    public float limitsMin;
    public float limitsMax;
    public float maxFrictionForce;
    public final MotorSettings swingMotorSettings = new MotorSettings();

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        sliderAxis = JoltUtils.readVec3(source);
        limitsMin = source.readFloat();
        limitsMax = source.readFloat();
        maxFrictionForce = source.readFloat();
        swingMotorSettings.restoreBinaryState(source);
    }
}
