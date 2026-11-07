package sh.adelessfox.odradek.game.ds2.middleware.riglogic.blendshapes;

import wtf.reversed.toolbox.collect.Shorts;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public record BlendShapes(
    Shorts lods,
    Shorts inputIndices,
    Shorts outputIndices
) {
    public static BlendShapes read(BinarySource source) throws IOException {
        return new BlendShapes(
            source.readShorts(source.readInt()),
            source.readShorts(source.readInt()),
            source.readShorts(source.readInt())
        );
    }
}
