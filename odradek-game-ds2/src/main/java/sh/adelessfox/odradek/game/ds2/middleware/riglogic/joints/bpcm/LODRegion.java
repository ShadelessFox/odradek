package sh.adelessfox.odradek.game.ds2.middleware.riglogic.joints.bpcm;

import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

record LODRegion(int size, int sizeAlignedToLastFullBlock, int sizeAlignedToSecondLastFullBlock) {
    public static LODRegion read(BinarySource source) throws IOException {
        return new LODRegion(
            source.readInt(),
            source.readInt(),
            source.readInt()
        );
    }
}
