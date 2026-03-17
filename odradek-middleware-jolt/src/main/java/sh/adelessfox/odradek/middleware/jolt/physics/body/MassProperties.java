package sh.adelessfox.odradek.middleware.jolt.physics.body;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.middleware.jolt.math.Mat44;

import java.io.IOException;

public record MassProperties(float mass, Mat44 inertia) {
    public static MassProperties restoreFromBinaryState(BinaryReader reader) throws IOException {
        var mass = reader.readFloat();
        var inertia = JoltUtils.readMatrix4(reader);

        return new MassProperties(mass, inertia);
    }
}
