package sh.adelessfox.atrac9j;

import sh.adelessfox.atrac9j.util.BitReader;

import java.util.Arrays;

final class ScaleFactors {
    static final byte[][] scaleFactorWeights = {
        {0, 0, 0, 1, 1, 2, 2, 2, 2, 2, 2, 3, 2, 3, 3, 4, 4, 4, 4, 4, 4, 5, 5, 6, 6, 7, 7, 8, 10, 12, 12, 12},
        {3, 2, 2, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 2, 3, 3, 4, 5, 7, 10, 10, 10},
        {0, 2, 4, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 8, 9, 12, 12, 12},
        {0, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 7, 8, 8, 10, 11, 11, 12, 13, 13, 13, 13},
        {0, 2, 2, 3, 3, 4, 4, 5, 4, 5, 5, 5, 5, 6, 7, 8, 8, 8, 8, 9, 9, 9, 10, 10, 11, 12, 12, 13, 13, 14, 14, 14},
        {1, 1, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 5, 6, 7, 7, 9, 11, 11, 11},
        {0, 5, 8, 10, 11, 11, 12, 12, 12, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 13, 15, 15, 15},
        {0, 2, 3, 4, 5, 6, 6, 7, 7, 8, 8, 8, 9, 9, 10, 10, 10, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 13, 13, 15, 15, 15}
    };

    private ScaleFactors() {
    }

    static void read(BitReader reader, Channel channel) {
        Arrays.fill(channel.scaleFactors, 0);

        channel.scaleFactorCodingMode = reader.readInt(2);
        if (channel.channelIndex == 0) {
            switch (channel.scaleFactorCodingMode) {
                case 0:
                    readVlcDeltaOffset(reader, channel);
                    break;
                case 1:
                    readClcOffset(reader, channel);
                    break;
                case 2:
                    if (channel.block.firstInSuperframe) {
                        throw new IllegalArgumentException();
                    }
                    readVlcDistanceToBaseline(reader, channel, channel.ScaleFactorsPrev, channel.block.quantizationUnitsPrev);
                    break;
                case 3:
                    if (channel.block.firstInSuperframe) {
                        throw new IllegalArgumentException();
                    }
                    readVlcDeltaOffsetWithBaseline(reader, channel, channel.ScaleFactorsPrev, channel.block.quantizationUnitsPrev);
                    break;
            }
        } else {
            switch (channel.scaleFactorCodingMode) {
                case 0:
                    readVlcDeltaOffset(reader, channel);
                    break;
                case 1:
                    readVlcDistanceToBaseline(reader, channel, channel.block.channels[0].scaleFactors, channel.block.extensionUnit);
                    break;
                case 2:
                    readVlcDeltaOffsetWithBaseline(reader, channel, channel.block.channels[0].scaleFactors, channel.block.extensionUnit);
                    break;
                case 3:
                    if (channel.block.firstInSuperframe) {
                        throw new IllegalArgumentException();
                    }
                    readVlcDistanceToBaseline(reader, channel, channel.ScaleFactorsPrev, channel.block.quantizationUnitsPrev);
                    break;
            }
        }

        for (int i = 0; i < channel.block.extensionUnit; i++) {
            if (channel.scaleFactors[i] < 0 || channel.scaleFactors[i] > 31) {
                throw new IllegalArgumentException("Scale factor values are out of range.");
            }
        }

        System.arraycopy(channel.scaleFactors, 0, channel.ScaleFactorsPrev, 0, channel.scaleFactors.length);
    }

    private static void readClcOffset(BitReader reader, Channel channel) {
        int maxBits = 5;
        int[] sf = channel.scaleFactors;
        int bitLength = reader.readInt(2) + 2;
        int baseValue = bitLength < maxBits ? reader.readInt(maxBits) : 0;

        for (int i = 0; i < channel.block.extensionUnit; i++) {
            sf[i] = reader.readInt(bitLength) + baseValue;
        }
    }

    private static void readVlcDeltaOffset(BitReader reader, Channel channel) {
        int weightIndex = reader.readInt(3);
        byte[] weights = scaleFactorWeights[weightIndex];

        int[] sf = channel.scaleFactors;
        int baseValue = reader.readInt(5);
        int bitLength = reader.readInt(2) + 3;
        HuffmanCodebook codebook = Tables.huffmanScaleFactorsUnsigned[bitLength];

        sf[0] = reader.readInt(bitLength);

        for (int i = 1; i < channel.block.extensionUnit; i++) {
            int delta = Unpack.readHuffmanValue(codebook, reader, false);
            sf[i] = (sf[i - 1] + delta) & (codebook.valueMax() - 1);
        }

        for (int i = 0; i < channel.block.extensionUnit; i++) {
            sf[i] += baseValue - weights[i];
        }
    }

    private static void readVlcDistanceToBaseline(BitReader reader, Channel channel, int[] baseline, int baselineLength) {
        int[] sf = channel.scaleFactors;
        int bitLength = reader.readInt(2) + 2;
        HuffmanCodebook codebook = Tables.huffmanScaleFactorsSigned[bitLength];
        int unitCount = Math.min(channel.block.extensionUnit, baselineLength);

        for (int i = 0; i < unitCount; i++) {
            int distance = Unpack.readHuffmanValue(codebook, reader, true);
            sf[i] = (baseline[i] + distance) & 31;
        }

        for (int i = unitCount; i < channel.block.extensionUnit; i++) {
            sf[i] = reader.readInt(5);
        }
    }

    private static void readVlcDeltaOffsetWithBaseline(BitReader reader, Channel channel, int[] baseline, int baselineLength) {
        int[] sf = channel.scaleFactors;
        int baseValue = reader.readOffsetBinary(5);
        int bitLength = reader.readInt(2) + 1;
        HuffmanCodebook codebook = Tables.huffmanScaleFactorsUnsigned[bitLength];
        int unitCount = Math.min(channel.block.extensionUnit, baselineLength);

        sf[0] = reader.readInt(bitLength);

        for (int i = 1; i < unitCount; i++) {
            int delta = Unpack.readHuffmanValue(codebook, reader, false);
            sf[i] = (sf[i - 1] + delta) & (codebook.valueMax() - 1);
        }

        for (int i = 0; i < unitCount; i++) {
            sf[i] += baseValue + baseline[i];
        }

        for (int i = unitCount; i < channel.block.extensionUnit; i++) {
            sf[i] = reader.readInt(5);
        }
    }
}

