package sh.adelessfox.odradek.game.hfw.data.riglogic.conditionaltable;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record ConditionalTable(
    short[] inputIndices,
    short[] outputIndices,
    float[] fromValues,
    float[] toValues,
    float[] slopeValues,
    float[] cutValues,
    short inputCount,
    short outputCount
) {
    public static ConditionalTable read(BinaryReader reader) throws IOException {
        return new ConditionalTable(
            reader.readShorts(reader.readInt()),
            reader.readShorts(reader.readInt()),
            reader.readFloats(reader.readInt()),
            reader.readFloats(reader.readInt()),
            reader.readFloats(reader.readInt()),
            reader.readFloats(reader.readInt()),
            reader.readShort(),
            reader.readShort()
        );
    }
}
