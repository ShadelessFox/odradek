package sh.adelessfox.odradek.game.ds2.middleware.riglogic;

import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

import java.io.IOException;

public record Configuration(
    CalculationType calculationType,
    boolean loadJoints,
    boolean loadBlendShapes,
    boolean loadAnimatedMaps,
    boolean loadMachineLearnedBehavior
) {
    public static Configuration read(BinarySource source) throws IOException {
        return new Configuration(
            CalculationType.values()[source.readInt()],
            source.readBool(BoolFormat.BYTE),
            source.readBool(BoolFormat.BYTE),
            source.readBool(BoolFormat.BYTE),
            source.readBool(BoolFormat.BYTE)
        );
    }
}
