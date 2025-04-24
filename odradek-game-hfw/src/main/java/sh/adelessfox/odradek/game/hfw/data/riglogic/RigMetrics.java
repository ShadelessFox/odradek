package sh.adelessfox.odradek.game.hfw.data.riglogic;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record RigMetrics(
    short lodCount,
    short guiControlCount,
    short rawControlCount,
    short psdCount,
    short jointAttributeCount,
    short blendShapeCount,
    short animatedMapCount
) {
    public static RigMetrics read(BinaryReader reader) throws IOException {
        return new RigMetrics(
            reader.readShort(),
            reader.readShort(),
            reader.readShort(),
            reader.readShort(),
            reader.readShort(),
            reader.readShort(),
            reader.readShort()
        );
    }
}
