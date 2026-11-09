package sh.adelessfox.odradek.game.hfw.middleware.riglogic.controls;

import sh.adelessfox.odradek.game.hfw.middleware.riglogic.conditionaltable.ConditionalTable;
import sh.adelessfox.odradek.game.hfw.middleware.riglogic.psdmatrix.PSDMatrix;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public record Controls(
    ConditionalTable guiToRawMapping,
    PSDMatrix psds
) {
    public static Controls read(BinarySource source) throws IOException {
        return new Controls(
            ConditionalTable.read(source),
            PSDMatrix.read(source)
        );
    }
}
