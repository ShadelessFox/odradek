package sh.adelessfox.odradek.game.hfw.data.riglogic.joints.bpcm;

import sh.adelessfox.odradek.io.BinaryReader;

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
    public static JointGroup read(BinaryReader reader) throws IOException {
        return new JointGroup(
            reader.readInt(),
            reader.readInt(),
            reader.readInt(),
            reader.readInt(),
            reader.readInt(),
            reader.readInt(),
            reader.readInt(),
            reader.readInt()
        );
    }
}
