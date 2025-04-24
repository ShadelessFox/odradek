package sh.adelessfox.odradek.game.hfw.data.riglogic.joints.bpcm;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

record LODRegion(int size, int sizeAlignedToLastFullBlock, int sizeAlignedToSecondLastFullBlock) {
    public static LODRegion read(BinaryReader reader) throws IOException {
        return new LODRegion(
            reader.readInt(),
            reader.readInt(),
            reader.readInt()
        );
    }
}
