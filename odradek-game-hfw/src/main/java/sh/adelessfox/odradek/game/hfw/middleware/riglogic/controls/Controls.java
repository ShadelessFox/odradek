package sh.adelessfox.odradek.game.hfw.middleware.riglogic.controls;

import sh.adelessfox.odradek.game.hfw.middleware.riglogic.conditionaltable.ConditionalTable;
import sh.adelessfox.odradek.game.hfw.middleware.riglogic.psdmatrix.PSDMatrix;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record Controls(
    ConditionalTable guiToRawMapping,
    PSDMatrix psds
) {
    public static Controls read(BinaryReader reader) throws IOException {
        return new Controls(
            ConditionalTable.read(reader),
            PSDMatrix.read(reader)
        );
    }
}
