package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.constraints;

import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class MotorSettings {
    public float frequency;
    public float damping;
    public float minForceLimit;
    public float maxForceLimit;
    public float minTorqueLimit;
    public float maxTorqueLimit;

    public void restoreBinaryState(BinarySource source) throws IOException {
        frequency = source.readFloat();
        damping = source.readFloat();
        minForceLimit = source.readFloat();
        maxForceLimit = source.readFloat();
        minTorqueLimit = source.readFloat();
        maxTorqueLimit = source.readFloat();
    }
}
