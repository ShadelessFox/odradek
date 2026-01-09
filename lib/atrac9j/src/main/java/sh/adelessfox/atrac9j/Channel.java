package sh.adelessfox.atrac9j;

import sh.adelessfox.atrac9j.util.Mdct;

final class Channel {
    final Atrac9Config Config;
    final int ChannelIndex;
    final Block Block;
    final Mdct Mdct;

    final double[] Pcm = new double[256];
    final double[] Spectra = new double[256];

    int CodedQuantUnits;
    int ScaleFactorCodingMode;
    final int[] ScaleFactors = new int[31];
    final int[] ScaleFactorsPrev = new int[31];

    final int[] Precisions = new int[30];
    final int[] PrecisionsFine = new int[30];
    final int[] PrecisionMask = new int[30];

    final int[] SpectraValuesBuffer = new int[16];
    final int[] CodebookSet = new int[30];

    final int[] QuantizedSpectra = new int[256];
    final int[] QuantizedSpectraFine = new int[256];

    int BexMode;
    int BexValueCount;
    final int[] BexValues = new int[4];
    final double[] BexScales = new double[6];
    Atrac9Rng Rng;

    Channel(Block parentBlock, int channelIndex) {
        Block = parentBlock;
        ChannelIndex = channelIndex;
        Config = parentBlock.Config;
        Mdct = new Mdct(Config.FrameSamplesPower, Tables.ImdctWindow[Config.FrameSamplesPower - 6], 1);
    }

    boolean IsPrimary() {
        return Block.PrimaryChannelIndex == ChannelIndex;
    }

    void UpdateCodedUnits() {
        CodedQuantUnits = IsPrimary() ? Block.QuantizationUnitCount : Block.StereoQuantizationUnit;
    }
}
