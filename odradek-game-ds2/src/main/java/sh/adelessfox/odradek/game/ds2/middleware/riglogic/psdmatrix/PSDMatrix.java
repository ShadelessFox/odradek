package sh.adelessfox.odradek.game.ds2.middleware.riglogic.psdmatrix;

import wtf.reversed.toolbox.collect.Floats;
import wtf.reversed.toolbox.collect.Shorts;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public record PSDMatrix(
    short distinctPSDs,
    Shorts rowIndices,
    Shorts columnIndices,
    Floats values
) {
    public static PSDMatrix read(BinarySource source) throws IOException {
        return new PSDMatrix(
            source.readShort(),
            source.readShorts(source.readInt()),
            source.readShorts(source.readInt()),
            source.readFloats(source.readInt())
        );
    }
}
