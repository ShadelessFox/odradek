package sh.adelessfox.atrac9j;

import java.util.function.IntToDoubleFunction;

import static sh.adelessfox.atrac9j.HuffmanCodebooks.*;

final class Tables {
    private Tables() {
    }

    static final int[] sampleRates = {
        11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000,
        44100, 48000, 64000, 88200, 96000, 128000, 176400, 192000
    };

    static final byte[] samplingRateIndexToFrameSamplesPower = {6, 6, 7, 7, 7, 8, 8, 8, 6, 6, 7, 7, 7, 8, 8, 8};

    // From sampling rate index
    static final byte[] maxBandCount = {8, 8, 12, 12, 12, 18, 18, 18, 8, 8, 12, 12, 12, 16, 16, 16};
    static final byte[] bandToQuantUnitCount = {0, 4, 8, 10, 12, 13, 14, 15, 16, 18, 20, 21, 22, 23, 24, 25, 26, 28, 30};

    static final byte[] quantUnitToCoeffCount = {
        2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 8, 8, 8,
        8, 8, 8, 8, 8, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16
    };

    static final short[] quantUnitToCoeffIndex = {
        0, 2, 4, 6, 8, 10, 12, 14, 16, 20, 24, 28, 32, 40, 48, 56,
        64, 72, 80, 88, 96, 112, 128, 144, 160, 176, 192, 208, 224, 240, 256
    };

    static final byte[] quantUnitToCodebookIndex = {
        0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2,
        2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3
    };

    static final ChannelConfig[] channelConfig = {
        ChannelConfig.of(BlockType.MONO),
        ChannelConfig.of(BlockType.MONO, BlockType.MONO),
        ChannelConfig.of(BlockType.STEREO),
        ChannelConfig.of(BlockType.STEREO, BlockType.MONO, BlockType.LFE, BlockType.STEREO),
        ChannelConfig.of(BlockType.STEREO, BlockType.MONO, BlockType.LFE, BlockType.STEREO, BlockType.STEREO),
        ChannelConfig.of(BlockType.STEREO, BlockType.STEREO)
    };

    static final HuffmanCodebook[] huffmanScaleFactorsUnsigned =
        generateHuffmanCodebooks(huffmanScaleFactorsACodes, huffmanScaleFactorsABits, huffmanScaleFactorsGroupSizes);

    static final HuffmanCodebook[] huffmanScaleFactorsSigned =
        generateHuffmanCodebooks(huffmanScaleFactorsBCodes, huffmanScaleFactorsBBits, huffmanScaleFactorsGroupSizes);

    static final HuffmanCodebook[][][] huffmanSpectrum = {
        generateHuffmanCodebooks(huffmanSpectrumACodes, huffmanSpectrumABits, huffmanSpectrumAGroupSizes),
        generateHuffmanCodebooks(huffmanSpectrumBCodes, huffmanSpectrumBBits, huffmanSpectrumBGroupSizes)
    };

    static final double[][] imdctWindow = {generateImdctWindow(6), generateImdctWindow(7), generateImdctWindow(8)};

    static final double[] spectrumScale = generate(32, Tables::spectrumScaleFunction);
    static final double[] quantizerStepSize = generate(16, Tables::quantizerStepSizeFunction);
    static final double[] quantizerFineStepSize = generate(16, Tables::quantizerFineStepSizeFunction);

    static final byte[][] gradientCurves = BitAllocation.generateGradientCurves();

    static int maxHuffPrecision(boolean highSampleRate) {
        return highSampleRate ? 1 : 7;
    }

    static int minBandCount(boolean highSampleRate) {
        return highSampleRate ? 1 : 3;
    }

    static int maxExtensionBand(boolean highSampleRate) {
        return highSampleRate ? 16 : 18;
    }

    private static double quantizerStepSizeFunction(int x) {
        return 2.0 / ((1 << (x + 1)) - 1);
    }

    private static double quantizerFineStepSizeFunction(int x) {
        return quantizerStepSizeFunction(x) / 65535;
    }

    private static double spectrumScaleFunction(int x) {
        return Math.pow(2, x - 15);
    }

    private static double[] generateImdctWindow(int frameSizePower) {
        int frameSize = 1 << frameSizePower;
        double[] output = new double[frameSize];

        double[] window = generateMdctWindow(frameSizePower);
        for (int i = 0; i < frameSize; i++) {
            output[i] = window[i] / (window[frameSize - 1 - i] * window[frameSize - 1 - i] + window[i] * window[i]);
        }

        return output;
    }

    private static double[] generateMdctWindow(int frameSizePower) {
        int frameSize = 1 << frameSizePower;
        double[] output = new double[frameSize];

        for (int i = 0; i < frameSize; i++) {
            output[i] = (Math.sin(((i + 0.5) / frameSize - 0.5) * Math.PI) + 1.0) * 0.5;
        }

        return output;
    }

    private static double[] generate(int count, IntToDoubleFunction elementGenerator) {
        double[] table = new double[count];
        for (int i = 0; i < count; i++) {
            table[i] = elementGenerator.applyAsDouble(i);
        }
        return table;
    }
}
