package sh.adelessfox.atrac9j;

import sh.adelessfox.atrac9j.util.Mdct;

final class Channel {
    final Atrac9Config config;
    final int channelIndex;
    final Block block;
    final Mdct mdct;

    final double[] pcm = new double[256];
    final double[] spectra = new double[256];

    int codedQuantUnits;
    int scaleFactorCodingMode;
    final int[] scaleFactors = new int[31];
    final int[] ScaleFactorsPrev = new int[31];

    final int[] Precisions = new int[30];
    final int[] PrecisionsFine = new int[30];
    final int[] PrecisionMask = new int[30];

    final int[] SpectraValuesBuffer = new int[16];
    final int[] CodebookSet = new int[30];

    final int[] QuantizedSpectra = new int[256];
    final int[] QuantizedSpectraFine = new int[256];

    int bexMode;
    int bexValueCount;
    final int[] bexValues = new int[4];
    final double[] bexScales = new double[6];
    Atrac9Rng rng;

    Channel(Block parentBlock, int channelIndex) {
        this.block = parentBlock;
        this.channelIndex = channelIndex;
        this.config = parentBlock.config;
        this.mdct = new Mdct(config.frameSamplesPower(), Tables.imdctWindow[config.frameSamplesPower() - 6], 1);
    }

    boolean isPrimary() {
        return block.primaryChannelIndex == channelIndex;
    }

    void updateCodedUnits() {
        codedQuantUnits = isPrimary() ? block.quantizationUnitCount : block.stereoQuantizationUnit;
    }
}
