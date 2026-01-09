package sh.adelessfox.libatrac9;

import java.util.function.IntToDoubleFunction;

import static sh.adelessfox.libatrac9.HuffmanCodebooks.*;

final class Tables {
    private Tables() {
    }

    public static int MaxHuffPrecision(boolean highSampleRate) {
        return highSampleRate ? 1 : 7;
    }

    public static int MinBandCount(boolean highSampleRate) {
        return highSampleRate ? 1 : 3;
    }

    public static int MaxExtensionBand(boolean highSampleRate) {
        return highSampleRate ? 16 : 18;
    }

    public static final int[] SampleRates = {
        11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000,
        44100, 48000, 64000, 88200, 96000, 128000, 176400, 192000
    };

    public static final byte[] SamplingRateIndexToFrameSamplesPower = {6, 6, 7, 7, 7, 8, 8, 8, 6, 6, 7, 7, 7, 8, 8, 8};

    // From sampling rate index
    public static final byte[] MaxBandCount = {8, 8, 12, 12, 12, 18, 18, 18, 8, 8, 12, 12, 12, 16, 16, 16};
    public static final byte[] BandToQuantUnitCount = {0, 4, 8, 10, 12, 13, 14, 15, 16, 18, 20, 21, 22, 23, 24, 25, 26, 28, 30};

    public static final byte[] QuantUnitToCoeffCount = {
        2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 8, 8, 8,
        8, 8, 8, 8, 8, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16
    };

    public static final short[] QuantUnitToCoeffIndex = {
        0, 2, 4, 6, 8, 10, 12, 14, 16, 20, 24, 28, 32, 40, 48, 56,
        64, 72, 80, 88, 96, 112, 128, 144, 160, 176, 192, 208, 224, 240, 256
    };

    public static final byte[] QuantUnitToCodebookIndex = {
        0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2,
        2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3
    };

    public static final ChannelConfig[] ChannelConfig = {
        new ChannelConfig(BlockType.Mono),
        new ChannelConfig(BlockType.Mono, BlockType.Mono),
        new ChannelConfig(BlockType.Stereo),
        new ChannelConfig(BlockType.Stereo, BlockType.Mono, BlockType.LFE, BlockType.Stereo),
        new ChannelConfig(BlockType.Stereo, BlockType.Mono, BlockType.LFE, BlockType.Stereo, BlockType.Stereo),
        new ChannelConfig(BlockType.Stereo, BlockType.Stereo)
    };

    public static final HuffmanCodebook[] HuffmanScaleFactorsUnsigned =
        GenerateHuffmanCodebooks(HuffmanScaleFactorsACodes, HuffmanScaleFactorsABits, HuffmanScaleFactorsGroupSizes);

    public static final HuffmanCodebook[] HuffmanScaleFactorsSigned =
        GenerateHuffmanCodebooks(HuffmanScaleFactorsBCodes, HuffmanScaleFactorsBBits, HuffmanScaleFactorsGroupSizes);

    public static final HuffmanCodebook[][][] HuffmanSpectrum = {
        GenerateHuffmanCodebooks(HuffmanSpectrumACodes, HuffmanSpectrumABits, HuffmanSpectrumAGroupSizes),
        GenerateHuffmanCodebooks(HuffmanSpectrumBCodes, HuffmanSpectrumBBits, HuffmanSpectrumBGroupSizes)
    };

    public static final double[][] ImdctWindow = {GenerateImdctWindow(6), GenerateImdctWindow(7), GenerateImdctWindow(8)};

    public static final double[] SpectrumScale = Generate(32, Tables::SpectrumScaleFunction);
    public static final double[] QuantizerStepSize = Generate(16, Tables::QuantizerStepSizeFunction);
    public static final double[] QuantizerFineStepSize = Generate(16, Tables::QuantizerFineStepSizeFunction);

    public static final byte[][] GradientCurves = BitAllocation.GenerateGradientCurves();

    private static double QuantizerStepSizeFunction(int x) {
        return 2.0 / ((1 << (x + 1)) - 1);
    }

    private static double QuantizerFineStepSizeFunction(int x) {
        return QuantizerStepSizeFunction(x) / 65535/*ushort.MaxValue*/;
    }

    private static double SpectrumScaleFunction(int x) {
        return Math.pow(2, x - 15);
    }

    private static double[] GenerateImdctWindow(int frameSizePower) {
        int frameSize = 1 << frameSizePower;
        var output = new double[frameSize];

        double[] a1 = GenerateMdctWindow(frameSizePower);

        for (int i = 0; i < frameSize; i++) {
            output[i] = a1[i] / (a1[frameSize - 1 - i] * a1[frameSize - 1 - i] + a1[i] * a1[i]);
        }
        return output;
    }

    private static double[] GenerateMdctWindow(int frameSizePower) {
        int frameSize = 1 << frameSizePower;
        var output = new double[frameSize];

        for (int i = 0; i < frameSize; i++) {
            output[i] = (Math.sin(((i + 0.5) / frameSize - 0.5) * Math.PI) + 1.0) * 0.5;
        }

        return output;
    }

    private static double[] Generate(int count, IntToDoubleFunction elementGenerator) {
        var table = new double[count];
        for (int i = 0; i < count; i++) {
            table[i] = elementGenerator.applyAsDouble(i);
        }
        return table;
    }


}
