package sh.adelessfox.odradek.game.hfw.data.riglogic.joints;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record Joints(
    JointsEvaluator evaluator,
    float[] neutralValues,
    short[][] variableAttributeIndices,
    short jointGroupCount
) {
    public static Joints read(BinaryReader reader) throws IOException {
        return new Joints(
            JointsEvaluator.read(reader),
            reader.readFloats(reader.readInt()),
            reader.readObjects(reader.readInt(), r -> r.readShorts(r.readInt()), short[][]::new),
            reader.readShort()
        );
    }
}
