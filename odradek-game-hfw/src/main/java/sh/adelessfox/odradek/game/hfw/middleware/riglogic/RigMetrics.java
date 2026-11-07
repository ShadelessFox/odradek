package sh.adelessfox.odradek.game.hfw.middleware.riglogic;

import wtf.reversed.toolbox.io.BinarySource;

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
    public static RigMetrics read(BinarySource source) throws IOException {
        return new RigMetrics(
            source.readShort(),
            source.readShort(),
            source.readShort(),
            source.readShort(),
            source.readShort(),
            source.readShort(),
            source.readShort()
        );
    }
}
