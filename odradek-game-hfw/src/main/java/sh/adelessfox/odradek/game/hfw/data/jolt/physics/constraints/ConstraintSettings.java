package sh.adelessfox.odradek.game.hfw.data.jolt.physics.constraints;

import sh.adelessfox.odradek.game.hfw.data.jolt.core.Factory;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class ConstraintSettings {
    public float drawConstraintSize;

    public static ConstraintSettings restoreFromBinaryState(BinaryReader reader) throws IOException {
        var hash = reader.readInt();
        var name = Factory.getTypeName(hash);
        var result = switch (name) {
            case "HingeConstraintSettings" -> new HingeConstraintSettings();
            case "PointConstraintSettings" -> new PointConstraintSettings();
            case "SwingTwistConstraintSettings" -> new SwingTwistConstraintSettings();
            case "SliderConstraintSettings" -> new SliderConstraintSettings();
            default -> throw new UnsupportedOperationException("Constraint %s not implemented".formatted(name));
        };
        result.restoreBinaryState(reader);
        return result;
    }

    public void restoreBinaryState(BinaryReader reader) throws IOException {
        drawConstraintSize = reader.readFloat();
    }
}
