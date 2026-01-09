package sh.adelessfox.atrac9j;

import sh.adelessfox.atrac9j.util.BitReader;

import java.util.Arrays;

final class ScaleFactors {
    static final byte[][] ScaleFactorWeights = {
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

    static void Read(BitReader reader, Channel channel) {
        Arrays.fill(channel.ScaleFactors, 0);

        channel.ScaleFactorCodingMode = reader.ReadInt(2);
        if (channel.ChannelIndex == 0) {
            switch (channel.ScaleFactorCodingMode) {
                case 0:
                    ReadVlcDeltaOffset(reader, channel);
                    break;
                case 1:
                    ReadClcOffset(reader, channel);
                    break;
                case 2:
                    if (channel.Block.FirstInSuperframe) {
                        throw new IllegalArgumentException();
                    }
                    ReadVlcDistanceToBaseline(reader, channel, channel.ScaleFactorsPrev, channel.Block.QuantizationUnitsPrev);
                    break;
                case 3:
                    if (channel.Block.FirstInSuperframe) {
                        throw new IllegalArgumentException();
                    }
                    ReadVlcDeltaOffsetWithBaseline(reader, channel, channel.ScaleFactorsPrev, channel.Block.QuantizationUnitsPrev);
                    break;
            }
        } else {
            switch (channel.ScaleFactorCodingMode) {
                case 0:
                    ReadVlcDeltaOffset(reader, channel);
                    break;
                case 1:
                    ReadVlcDistanceToBaseline(reader, channel, channel.Block.Channels[0].ScaleFactors, channel.Block.ExtensionUnit);
                    break;
                case 2:
                    ReadVlcDeltaOffsetWithBaseline(reader, channel, channel.Block.Channels[0].ScaleFactors, channel.Block.ExtensionUnit);
                    break;
                case 3:
                    if (channel.Block.FirstInSuperframe) {
                        throw new IllegalArgumentException();
                    }
                    ReadVlcDistanceToBaseline(reader, channel, channel.ScaleFactorsPrev, channel.Block.QuantizationUnitsPrev);
                    break;
            }
        }

        for (int i = 0; i < channel.Block.ExtensionUnit; i++) {
            if (channel.ScaleFactors[i] < 0 || channel.ScaleFactors[i] > 31) {
                throw new IllegalArgumentException("Scale factor values are out of range.");
            }
        }

        System.arraycopy(channel.ScaleFactors, 0, channel.ScaleFactorsPrev, 0, channel.ScaleFactors.length);
    }

    private static void ReadClcOffset(BitReader reader, Channel channel) {
        int maxBits = 5;
        int[] sf = channel.ScaleFactors;
        int bitLength = reader.ReadInt(2) + 2;
        int baseValue = bitLength < maxBits ? reader.ReadInt(maxBits) : 0;

        for (int i = 0; i < channel.Block.ExtensionUnit; i++) {
            sf[i] = reader.ReadInt(bitLength) + baseValue;
        }
    }

    private static void ReadVlcDeltaOffset(BitReader reader, Channel channel) {
        int weightIndex = reader.ReadInt(3);
        byte[] weights = ScaleFactorWeights[weightIndex];

        int[] sf = channel.ScaleFactors;
        int baseValue = reader.ReadInt(5);
        int bitLength = reader.ReadInt(2) + 3;
        HuffmanCodebook codebook = Tables.HuffmanScaleFactorsUnsigned[bitLength];

        sf[0] = reader.ReadInt(bitLength);

        for (int i = 1; i < channel.Block.ExtensionUnit; i++) {
            int delta = Unpack.ReadHuffmanValue(codebook, reader, false);
            sf[i] = (sf[i - 1] + delta) & (codebook.ValueMax - 1);
        }

        for (int i = 0; i < channel.Block.ExtensionUnit; i++) {
            sf[i] += baseValue - weights[i];
        }
    }

    private static void ReadVlcDistanceToBaseline(BitReader reader, Channel channel, int[] baseline, int baselineLength) {
        int[] sf = channel.ScaleFactors;
        int bitLength = reader.ReadInt(2) + 2;
        HuffmanCodebook codebook = Tables.HuffmanScaleFactorsSigned[bitLength];
        int unitCount = Math.min(channel.Block.ExtensionUnit, baselineLength);

        for (int i = 0; i < unitCount; i++) {
            int distance = Unpack.ReadHuffmanValue(codebook, reader, true);
            sf[i] = (baseline[i] + distance) & 31;
        }

        for (int i = unitCount; i < channel.Block.ExtensionUnit; i++) {
            sf[i] = reader.ReadInt(5);
        }
    }

    private static void ReadVlcDeltaOffsetWithBaseline(BitReader reader, Channel channel, int[] baseline, int baselineLength) {
        int[] sf = channel.ScaleFactors;
        int baseValue = reader.ReadOffsetBinary(5);
        int bitLength = reader.ReadInt(2) + 1;
        HuffmanCodebook codebook = Tables.HuffmanScaleFactorsUnsigned[bitLength];
        int unitCount = Math.min(channel.Block.ExtensionUnit, baselineLength);

        sf[0] = reader.ReadInt(bitLength);

        for (int i = 1; i < unitCount; i++) {
            int delta = Unpack.ReadHuffmanValue(codebook, reader, false);
            sf[i] = (sf[i - 1] + delta) & (codebook.ValueMax - 1);
        }

        for (int i = 0; i < unitCount; i++) {
            sf[i] += baseValue + baseline[i];
        }

        for (int i = unitCount; i < channel.Block.ExtensionUnit; i++) {
            sf[i] = reader.ReadInt(5);
        }
    }
}

