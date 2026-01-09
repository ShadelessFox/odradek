package sh.adelessfox.libatrac9;

import sh.adelessfox.libatrac9.util.Mdct;

final class Channel {
    public Atrac9Config Config;
    public int ChannelIndex;
    public Block Block;
    public Mdct Mdct;

    public double[] Pcm = new double[256];
    public double[] Spectra = new double[256];

    public int CodedQuantUnits;
    public int ScaleFactorCodingMode;
    public int[] ScaleFactors = new int[31];
    public int[] ScaleFactorsPrev = new int[31];

    public int[] Precisions = new int[30];
    public int[] PrecisionsFine = new int[30];
    public int[] PrecisionMask = new int[30];

    public int[] SpectraValuesBuffer = new int[16];
    public int[] CodebookSet = new int[30];

    public int[] QuantizedSpectra = new int[256];
    public int[] QuantizedSpectraFine = new int[256];

    public int BexMode;
    public int BexValueCount;
    public int[] BexValues = new int[4];
    public double[] BexScales = new double[6];
    public Atrac9Rng Rng;

    public Channel(Block parentBlock, int channelIndex) {
        Block = parentBlock;
        ChannelIndex = channelIndex;
        Config = parentBlock.Config;
        Mdct = new Mdct(Config.FrameSamplesPower, Tables.ImdctWindow[Config.FrameSamplesPower - 6], 1);
    }

    public boolean IsPrimary() {
        return Block.PrimaryChannelIndex == ChannelIndex;
    }

    public void UpdateCodedUnits() {
        CodedQuantUnits = IsPrimary() ? Block.QuantizationUnitCount : Block.StereoQuantizationUnit;
    }
}
