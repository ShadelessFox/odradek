package sh.adelessfox.odradek.game.hfw.data.riglogic.blendshapes;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record BlendShapes(
    short[] lods,
    short[] inputIndices,
    short[] outputIndices
) {
    public static BlendShapes read(BinaryReader reader) throws IOException {
        return new BlendShapes(
            reader.readShorts(reader.readInt()),
            reader.readShorts(reader.readInt()),
            reader.readShorts(reader.readInt())
        );
    }
}
