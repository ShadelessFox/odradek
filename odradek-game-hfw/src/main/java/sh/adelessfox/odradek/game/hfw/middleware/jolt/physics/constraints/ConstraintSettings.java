package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.constraints;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.core.Factory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class ConstraintSettings {
    public float drawConstraintSize;

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
        drawConstraintSize = source.readFloat();
    }
}
