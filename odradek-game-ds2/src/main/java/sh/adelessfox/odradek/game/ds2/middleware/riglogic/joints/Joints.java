package sh.adelessfox.odradek.game.ds2.middleware.riglogic.joints;

import sh.adelessfox.odradek.game.ds2.middleware.riglogic.Configuration;
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
    public static Joints read(Configuration configuration, BinarySource source) throws IOException {
        return new Joints(
            configuration.loadJoints() ? JointsEvaluator.read(source) : null,
            source.readFloats(source.readInt()),
            source.readObjects(source.readInt(), r -> r.readShorts(r.readInt())),
            source.readShort()
        );
    }
}
