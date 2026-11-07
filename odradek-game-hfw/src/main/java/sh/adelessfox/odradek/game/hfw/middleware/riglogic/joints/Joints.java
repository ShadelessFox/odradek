package sh.adelessfox.odradek.game.hfw.middleware.riglogic.joints;

import wtf.reversed.toolbox.collect.Floats;
import wtf.reversed.toolbox.collect.Shorts;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.List;

public record Joints(
    JointsEvaluator evaluator,
    Floats neutralValues,
    List<Shorts> variableAttributeIndices,
    short jointGroupCount
) {
    public static Joints read(BinarySource source) throws IOException {
        return new Joints(
            JointsEvaluator.read(source),
            source.readFloats(source.readInt()),
            source.readObjects(source.readInt(), r -> r.readShorts(r.readInt())),
            source.readShort()
        );
    }
}
