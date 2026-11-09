package sh.adelessfox.odradek.game.hfw.middleware.riglogic.animatedmaps;

import sh.adelessfox.odradek.game.hfw.middleware.riglogic.conditionaltable.ConditionalTable;
import wtf.reversed.toolbox.collect.Shorts;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public record AnimatedMaps(
    Shorts lods,
    ConditionalTable conditionals
) {
    public static AnimatedMaps read(BinarySource source) throws IOException {
        return new AnimatedMaps(
            source.readShorts(source.readInt()),
            ConditionalTable.read(source)
        );
    }
}
