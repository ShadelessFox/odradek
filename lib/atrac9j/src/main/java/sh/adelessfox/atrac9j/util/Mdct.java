package sh.adelessfox.atrac9j.util;

import java.util.ArrayList;
import java.util.List;

public final class Mdct {
    private final int MdctBits;
    private final int MdctSize;
    private final double Scale;

    private static final Object TableLock = new Object();
    private static int _tableBits = -1;
    private static final List<double[]> SinTables = new ArrayList<>();
    private static final List<double[]> CosTables = new ArrayList<>();
    private static final List<int[]> ShuffleTables = new ArrayList<>();

    private final double[] _imdctPrevious;
    private final double[] _imdctWindow;

    private final double[] _scratchMdct;
    private final double[] _scratchDct;

    public Mdct(int mdctBits, double[] window, double scale/* = 1*/) {
        SetTables(mdctBits);

        MdctBits = mdctBits;
        MdctSize = 1 << mdctBits;
        Scale = scale;

        if (window.length < MdctSize) {
            throw new IllegalArgumentException("Window must be as long as the MDCT size.");
        }

        _imdctPrevious = new double[MdctSize];
        _scratchMdct = new double[MdctSize];
        _scratchDct = new double[MdctSize];
        _imdctWindow = window;
    }

    private static void SetTables(int maxBits) {
        synchronized (TableLock) {
            if (maxBits > _tableBits) {
                for (int i = _tableBits + 1; i <= maxBits; i++) {
                    double[][] sin = new double[1][];
                    double[][] cos = new double[1][];
                    GenerateTrigTables(i, sin, cos);
                    SinTables.add(sin[0]);
                    CosTables.add(cos[0]);
                    ShuffleTables.add(GenerateShuffleTable(i));
                }
                _tableBits = maxBits;
            }
        }
    }

    public void RunImdct(double[] input, double[] output) {
        if (input.length < MdctSize) {
            throw new IllegalArgumentException("Input must be as long as the MDCT size.");
        }

        if (output.length < MdctSize) {
            throw new IllegalArgumentException("Output must be as long as the MDCT size.");
        }

        int size = MdctSize;
        int half = size / 2;
        double[] dctOut = _scratchMdct;

        Dct4(input, dctOut);

        for (int i = 0; i < half; i++) {
            output[i] = _imdctWindow[i] * dctOut[i + half] + _imdctPrevious[i];
            output[i + half] = _imdctWindow[i + half] * -dctOut[size - 1 - i] - _imdctPrevious[i + half];
            _imdctPrevious[i] = _imdctWindow[size - 1 - i] * -dctOut[half - i - 1];
            _imdctPrevious[i + half] = _imdctWindow[half - i - 1] * dctOut[i];
        }
    }

    /**
     * Does a Type-4 DCT.
     *
     * @param input  The input array containing the time or frequency-domain samples
     * @param output The output array that will contain the transformed time or frequency-domain samples
     */
    private void Dct4(double[] input, double[] output) {
        int[] shuffleTable = ShuffleTables.get(MdctBits);
        double[] sinTable = SinTables.get(MdctBits);
        double[] cosTable = CosTables.get(MdctBits);
        double[] dctTemp = _scratchDct;

        int size = MdctSize;
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
        int stageCount = MdctBits - 1;

        for (int stage = 0; stage < stageCount; stage++) {
            int blockCount = 1 << stage;
            int blockSizeBits = stageCount - stage;
            int blockHalfSizeBits = blockSizeBits - 1;
            int blockSize = 1 << blockSizeBits;
            int blockHalfSize = 1 << blockHalfSizeBits;
            sinTable = SinTables.get(blockHalfSizeBits);
            cosTable = CosTables.get(blockHalfSizeBits);

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

        for (int i = 0; i < MdctSize; i++) {
            output[i] = dctTemp[shuffleTable[i]] * Scale;
        }
    }

    private static void GenerateTrigTables(int sizeBits, double[][] sin, double[][] cos) {
        int size = 1 << sizeBits;
        sin[0] = new double[size];
        cos[0] = new double[size];

        for (int i = 0; i < size; i++) {
            double value = Math.PI * (4 * i + 1) / (4 * size);
            sin[0][i] = Math.sin(value);
            cos[0][i] = Math.cos(value);
        }
    }

    private static int[] GenerateShuffleTable(int sizeBits) {
        int size = 1 << sizeBits;
        var table = new int[size];

        for (int i = 0; i < size; i++) {
            table[i] = Bit.BitReverse32(i ^ (i / 2), sizeBits);
        }

        return table;
    }
}
