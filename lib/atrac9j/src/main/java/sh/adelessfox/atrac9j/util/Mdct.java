package sh.adelessfox.atrac9j.util;

import java.util.ArrayList;
import java.util.List;

public final class Mdct {
    private static final List<double[]> sinTables = new ArrayList<>();
    private static final List<double[]> cosTables = new ArrayList<>();
    private static final List<int[]> shuffleTables = new ArrayList<>();
    private static int tableBits = -1;

    private final int mdctBits;
    private final int mdctSize;
    private final double scale;

    private final double[] imdctPrevious;
    private final double[] imdctWindow;

    private final double[] sscratchMdct;
    private final double[] scratchDct;

    public Mdct(int mdctBits, double[] window, double scale/* = 1*/) {
        this.mdctBits = mdctBits;
        this.mdctSize = 1 << mdctBits;
        this.scale = scale;

        if (window.length < mdctSize) {
            throw new IllegalArgumentException("Window must be as long as the MDCT size.");
        }

        setTables(mdctBits);

        imdctPrevious = new double[mdctSize];
        sscratchMdct = new double[mdctSize];
        scratchDct = new double[mdctSize];
        imdctWindow = window;
    }

    private static synchronized void setTables(int maxBits) {
        if (maxBits > tableBits) {
            for (int sizeBits = tableBits + 1; sizeBits <= maxBits; sizeBits++) {
                int size = 1 << sizeBits;
                double[] sin = new double[size];
                double[] cos = new double[size];
                GenerateTrigTables(size, sin, cos);
                sinTables.add(sin);
                cosTables.add(cos);
                shuffleTables.add(GenerateShuffleTable(sizeBits));
            }
            tableBits = maxBits;
        }
    }

    public void runImdct(double[] input, double[] output) {
        if (input.length < mdctSize) {
            throw new IllegalArgumentException("Input must be as long as the MDCT size.");
        }

        if (output.length < mdctSize) {
            throw new IllegalArgumentException("Output must be as long as the MDCT size.");
        }

        int size = mdctSize;
        int half = size / 2;
        double[] dctOut = sscratchMdct;

        dct4(input, dctOut);

        for (int i = 0; i < half; i++) {
            output[i] = imdctWindow[i] * dctOut[i + half] + imdctPrevious[i];
            output[i + half] = imdctWindow[i + half] * -dctOut[size - 1 - i] - imdctPrevious[i + half];
            imdctPrevious[i] = imdctWindow[size - 1 - i] * -dctOut[half - i - 1];
            imdctPrevious[i + half] = imdctWindow[half - i - 1] * dctOut[i];
        }
    }

    /**
     * Does a Type-4 DCT.
     *
     * @param input  The input array containing the time or frequency-domain samples
     * @param output The output array that will contain the transformed time or frequency-domain samples
     */
    private void dct4(double[] input, double[] output) {
        int[] shuffleTable = shuffleTables.get(mdctBits);
        double[] sinTable = sinTables.get(mdctBits);
        double[] cosTable = cosTables.get(mdctBits);
        double[] dctTemp = scratchDct;

        int size = mdctSize;
        int lastIndex = size - 1;
        int halfSize = size / 2;
        for (int i = 0; i < halfSize; i++) {
            int i2 = i * 2;
            double a = input[i2];
            double b = input[lastIndex - i2];
            double sin = sinTable[i];
            double cos = cosTable[i];
            dctTemp[i2] = a * cos + b * sin;
            dctTemp[i2 + 1] = a * sin - b * cos;
        }

        int stageCount = mdctBits - 1;
        for (int stage = 0; stage < stageCount; stage++) {
            int blockCount = 1 << stage;
            int blockSizeBits = stageCount - stage;
            int blockHalfSizeBits = blockSizeBits - 1;
            int blockSize = 1 << blockSizeBits;
            int blockHalfSize = 1 << blockHalfSizeBits;

            sinTable = sinTables.get(blockHalfSizeBits);
            cosTable = cosTables.get(blockHalfSizeBits);

            for (int block = 0; block < blockCount; block++) {
                for (int i = 0; i < blockHalfSize; i++) {
                    int frontPos = (block * blockSize + i) * 2;
                    int backPos = frontPos + blockSize;
                    double a = dctTemp[frontPos] - dctTemp[backPos];
                    double b = dctTemp[frontPos + 1] - dctTemp[backPos + 1];
                    double sin = sinTable[i];
                    double cos = cosTable[i];
                    dctTemp[frontPos] += dctTemp[backPos];
                    dctTemp[frontPos + 1] += dctTemp[backPos + 1];
                    dctTemp[backPos] = a * cos + b * sin;
                    dctTemp[backPos + 1] = a * sin - b * cos;
                }
            }
        }

        for (int i = 0; i < mdctSize; i++) {
            output[i] = dctTemp[shuffleTable[i]] * scale;
        }
    }

    private static void GenerateTrigTables(int size, double[] sin, double[] cos) {
        for (int i = 0; i < size; i++) {
            double value = Math.PI * (4 * i + 1) / (4 * size);
            sin[i] = Math.sin(value);
            cos[i] = Math.cos(value);
        }
    }

    private static int[] GenerateShuffleTable(int sizeBits) {
        int size = 1 << sizeBits;
        var table = new int[size];

        for (int i = 0; i < size; i++) {
            table[i] = Bit.bitReverse32(i ^ (i / 2), sizeBits);
        }

        return table;
    }
}
