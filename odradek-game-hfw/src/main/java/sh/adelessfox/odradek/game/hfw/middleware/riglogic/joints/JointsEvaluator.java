package sh.adelessfox.odradek.game.hfw.middleware.riglogic.joints;

import sh.adelessfox.odradek.game.hfw.middleware.riglogic.joints.bpcm.JointStorage;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public record JointsEvaluator(
    JointStorage storage
) {
    public static JointsEvaluator read(BinarySource source) throws IOException {
        return new JointsEvaluator(JointStorage.read(source));
    }
}
