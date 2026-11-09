package sh.adelessfox.odradek.game.ds2.middleware.riglogic.joints.bpcm;

import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

record JointGroup(
    int valuesOffset,
    int inputIndicesOffset,
    int outputIndicesOffset,
    int lodsOffset,
    int valuesSize,
    int inputIndicesSize,
    int inputIndicesSizeAlignedTo4,
    int inputIndicesSizeAlignedTo8
) {
    public static JointGroup read(BinarySource source) throws IOException {
        return new JointGroup(
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readInt()
        );
    }
}
