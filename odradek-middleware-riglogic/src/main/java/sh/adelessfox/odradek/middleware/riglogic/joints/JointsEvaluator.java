package sh.adelessfox.odradek.middleware.riglogic.joints;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.riglogic.joints.bpcm.JointStorage;

import java.io.IOException;

public record JointsEvaluator(
    JointStorage storage
) {
    public static JointsEvaluator read(BinaryReader reader) throws IOException {
        return new JointsEvaluator(JointStorage.read(reader));
    }
}
