package sh.adelessfox.odradek.game.hfw.middleware.riglogic.joints;

import sh.adelessfox.odradek.game.hfw.middleware.riglogic.joints.bpcm.JointStorage;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record JointsEvaluator(
    JointStorage storage
) {
    public static JointsEvaluator read(BinaryReader reader) throws IOException {
        return new JointsEvaluator(JointStorage.read(reader));
    }
}
