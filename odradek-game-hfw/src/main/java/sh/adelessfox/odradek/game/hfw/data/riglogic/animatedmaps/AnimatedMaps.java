package sh.adelessfox.odradek.game.hfw.data.riglogic.animatedmaps;

import sh.adelessfox.odradek.game.hfw.data.riglogic.conditionaltable.ConditionalTable;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record AnimatedMaps(
    short[] lods,
    ConditionalTable conditionals
) {
    public static AnimatedMaps read(BinaryReader reader) throws IOException {
        return new AnimatedMaps(
            reader.readShorts(reader.readInt()),
            ConditionalTable.read(reader)
        );
    }
}
