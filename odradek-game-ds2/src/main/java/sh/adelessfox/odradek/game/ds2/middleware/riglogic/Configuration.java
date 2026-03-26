package sh.adelessfox.odradek.game.ds2.middleware.riglogic;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record Configuration(
    CalculationType calculationType,
    boolean loadJoints,
    boolean loadBlendShapes,
    boolean loadAnimatedMaps,
    boolean loadMachineLearnedBehavior
) {
    public static Configuration read(BinaryReader reader) throws IOException {
        return new Configuration(
            CalculationType.values()[reader.readInt()],
            reader.readByteBoolean(),
            reader.readByteBoolean(),
            reader.readByteBoolean(),
            reader.readByteBoolean()
        );
    }
}
