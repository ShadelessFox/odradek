package sh.adelessfox.odradek.game.hfw.middleware.riglogic;

import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public record Configuration(
    CalculationType calculationType
) {
    public static Configuration read(BinarySource source) throws IOException {
        return new Configuration(
            CalculationType.values()[source.readInt()]
        );
    }

}
