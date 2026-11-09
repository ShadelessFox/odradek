package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.constraints;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.core.Factory;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

import java.io.IOException;

public class ConstraintSettings {
    public boolean enabled;
    public float drawConstraintSize;
    public int constraintPriority;
    public int numVelocityStepsOverride;
    public int numPositionStepsOverride;

    public static ConstraintSettings restoreFromBinaryState(BinarySource source) throws IOException {
        var hash = source.readInt();
        var name = Factory.getTypeName(hash);
        var result = switch (name) {
            case "HingeConstraintSettings" -> new HingeConstraintSettings();
            case "PointConstraintSettings" -> new PointConstraintSettings();
            case "SwingTwistConstraintSettings" -> new SwingTwistConstraintSettings();
            case "SliderConstraintSettings" -> new SliderConstraintSettings();
            default -> throw new UnsupportedOperationException("Constraint %s not implemented".formatted(name));
        };
        result.restoreBinaryState(source);
        return result;
    }

    public void restoreBinaryState(BinarySource source) throws IOException {
        enabled = source.readBool(BoolFormat.BYTE);
        drawConstraintSize = source.readFloat();
        constraintPriority = source.readInt();
        numVelocityStepsOverride = source.readInt();
        numPositionStepsOverride = source.readInt();
    }
}
