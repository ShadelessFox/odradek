package sh.adelessfox.odradek.middleware.riglogic.controls;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.riglogic.conditionaltable.ConditionalTable;
import sh.adelessfox.odradek.middleware.riglogic.psdmatrix.PSDMatrix;

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
