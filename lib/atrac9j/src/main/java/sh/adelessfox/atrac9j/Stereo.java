package sh.adelessfox.atrac9j;

final class Stereo {
    private Stereo() {
    }

    static void applyIntensityStereo(Block block) {
        if (block.blockType != BlockType.STEREO) {
            return;
        }

        int totalUnits = block.quantizationUnitCount;
        int stereoUnits = block.stereoQuantizationUnit;
        if (stereoUnits >= totalUnits) {
            return;
        }

        Channel source = block.primaryChannel();
        Channel dest = block.secondaryChannel();

        for (int i = stereoUnits; i < totalUnits; i++) {
            int sign = block.jointStereoSigns[i];
            for (int sb = Tables.quantUnitToCoeffIndex[i]; sb < Tables.quantUnitToCoeffIndex[i + 1]; sb++) {
                if (sign > 0) {
                    dest.spectra[sb] = -source.spectra[sb];
                } else {
                    dest.spectra[sb] = source.spectra[sb];
                }
            }
        }
    }
}

