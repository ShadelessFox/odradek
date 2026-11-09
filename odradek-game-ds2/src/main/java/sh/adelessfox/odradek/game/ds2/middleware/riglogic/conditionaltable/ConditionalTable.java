package sh.adelessfox.odradek.game.ds2.middleware.riglogic.conditionaltable;

import wtf.reversed.toolbox.collect.Floats;
import wtf.reversed.toolbox.collect.Shorts;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public record ConditionalTable(
    Shorts intervalsRemaining,
    Shorts inputIndices,
    Shorts outputIndices,
    Floats fromValues,
    Floats toValues,
    Floats slopeValues,
    Floats cutValues,
    short inputCount,
    short outputCount
) {
    public static ConditionalTable read(BinarySource source) throws IOException {
        return new ConditionalTable(
            source.readShorts(source.readInt()),
            source.readShorts(source.readInt()),
            source.readShorts(source.readInt()),
            source.readFloats(source.readInt()),
            source.readFloats(source.readInt()),
            source.readFloats(source.readInt()),
            source.readFloats(source.readInt()),
            source.readShort(),
            source.readShort()
        );
    }
}
