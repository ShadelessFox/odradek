package sh.adelessfox.odradek.game.hfw.middleware.riglogic.joints;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;

public record Joints(
    JointsEvaluator evaluator,
    float[] neutralValues,
    List<short[]> variableAttributeIndices,
    short jointGroupCount
) {
    public static Joints read(BinaryReader reader) throws IOException {
        return new Joints(
            JointsEvaluator.read(reader),
            reader.readFloats(reader.readInt()),
            reader.readObjects(reader.readInt(), r -> r.readShorts(r.readInt())),
            reader.readShort()
        );
    }
}
