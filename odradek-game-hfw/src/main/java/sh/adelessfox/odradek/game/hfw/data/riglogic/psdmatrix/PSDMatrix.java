package sh.adelessfox.odradek.game.hfw.data.riglogic.psdmatrix;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record PSDMatrix(
    short distinctPSDs,
    short[] rowIndices,
    short[] columnIndices,
    float[] values
) {
    public static PSDMatrix read(BinaryReader reader) throws IOException {
        return new PSDMatrix(
            reader.readShort(),
            reader.readShorts(reader.readInt()),
            reader.readShorts(reader.readInt()),
            reader.readFloats(reader.readInt())
        );
    }
}
