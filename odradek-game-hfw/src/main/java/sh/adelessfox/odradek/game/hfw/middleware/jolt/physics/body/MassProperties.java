package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.body;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.math.Mat44;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public record MassProperties(float mass, Mat44 inertia) {
    public static MassProperties restoreFromBinaryState(BinarySource source) throws IOException {
        var mass = source.readFloat();
        var inertia = JoltUtils.readMatrix4(source);

        return new MassProperties(mass, inertia);
    }
}
