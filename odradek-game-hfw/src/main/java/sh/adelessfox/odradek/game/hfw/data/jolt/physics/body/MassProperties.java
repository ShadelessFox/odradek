package sh.adelessfox.odradek.game.hfw.data.jolt.physics.body;

import sh.adelessfox.odradek.game.hfw.data.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Mat44;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record MassProperties(float mass, Mat44 inertia) {
    public static MassProperties restoreFromBinaryState(BinaryReader reader) throws IOException {
        var mass = reader.readFloat();
        var inertia = JoltUtils.readMatrix4(reader);

        return new MassProperties(mass, inertia);
    }
}
