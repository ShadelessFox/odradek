package sh.adelessfox.odradek.game.ds2.middleware.riglogic.joints;

import sh.adelessfox.odradek.game.ds2.middleware.riglogic.Configuration;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record Joints(
    JointsEvaluator evaluator,
    float[] neutralValues,
    short[][] variableAttributeIndices,
    short jointGroupCount
) {
    public static Joints read(Configuration configuration, BinaryReader reader) throws IOException {
        return new Joints(
            configuration.loadJoints() ? JointsEvaluator.read(reader) : null,
            reader.readFloats(reader.readInt()),
            reader.readObjects(reader.readInt(), r -> r.readShorts(r.readInt()), short[][]::new),
            reader.readShort()
        );
    }
}
