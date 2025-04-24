package sh.adelessfox.odradek.game.hfw.data.riglogic;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record Configuration(
    CalculationType calculationType
) {
    public static Configuration read(BinaryReader reader) throws IOException {
        return new Configuration(
            CalculationType.values()[reader.readInt()]
        );
    }

}
