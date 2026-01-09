package sh.adelessfox.atrac9j;

import sh.adelessfox.atrac9j.util.Mdct;

final class Channel {
    Atrac9Config Config;
    int ChannelIndex;
    Block Block;
    Mdct Mdct;

    double[] Pcm = new double[256];
    double[] Spectra = new double[256];

    int CodedQuantUnits;
    int ScaleFactorCodingMode;
    int[] ScaleFactors = new int[31];
    int[] ScaleFactorsPrev = new int[31];

    int[] Precisions = new int[30];
    int[] PrecisionsFine = new int[30];
    int[] PrecisionMask = new int[30];

    int[] SpectraValuesBuffer = new int[16];
    int[] CodebookSet = new int[30];

    int[] QuantizedSpectra = new int[256];
    int[] QuantizedSpectraFine = new int[256];

    int BexMode;
    int BexValueCount;
    int[] BexValues = new int[4];
    double[] BexScales = new double[6];
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
