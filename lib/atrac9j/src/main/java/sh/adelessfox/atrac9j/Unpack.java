package sh.adelessfox.atrac9j;

import sh.adelessfox.atrac9j.util.Bit;
import sh.adelessfox.atrac9j.util.BitReader;

import java.util.Arrays;

final class Unpack {
    private Unpack() {
    }

    static void unpackFrame(BitReader reader, Frame frame, int frameIndex) {
        for (Block block : frame.blocks()) {
            unpackBlock(reader, block, frameIndex);
        }
    }

    private static void unpackBlock(BitReader reader, Block block, int frameIndex) {
        readBlockHeader(reader, block, frameIndex);

        if (block.blockType == BlockType.LFE) {
            unpackLfeBlock(reader, block);
        } else {
            unpackStandardBlock(reader, block);
        }

        reader.align(8);
    }

    private static void readBlockHeader(BitReader reader, Block block, int frameIndex) {
        boolean firstInSuperframe = frameIndex == 0;
        block.firstInSuperframe = !reader.readBool();
        block.reuseBandParams = reader.readBool();

        if (block.firstInSuperframe != firstInSuperframe) {
            throw new IllegalArgumentException();
        }

        if (firstInSuperframe && block.reuseBandParams && block.blockType != BlockType.LFE) {
            throw new IllegalArgumentException();
        }
    }

    private static void unpackStandardBlock(BitReader reader, Block block) {
        Channel[] channels = block.channels;

        if (!block.reuseBandParams) {
            readBandParams(reader, block);
        }

        readGradientParams(reader, block);
        BitAllocation.createGradient(block);
        readStereoParams(reader, block);
        readExtensionParams(reader, block);

        for (Channel channel : channels) {
            channel.updateCodedUnits();

            ScaleFactors.read(reader, channel);
            BitAllocation.calculateMask(channel);
            BitAllocation.calculatePrecisions(channel);
            calculateSpectrumCodebookIndex(channel);

            readSpectra(reader, channel);
            readSpectraFine(reader, channel);
        }

        block.quantizationUnitsPrev = block.bandExtensionEnabled ? block.extensionUnit : block.quantizationUnitCount;
    }

    private static void readBandParams(BitReader reader, Block block) {
        int minBandCount = Tables.minBandCount(block.config.highSampleRate());
        int maxExtensionBand = Tables.maxExtensionBand(block.config.highSampleRate());
        block.bandCount = reader.readInt(4);
        block.bandCount += minBandCount;
        block.quantizationUnitCount = Tables.bandToQuantUnitCount[block.bandCount];
        if (block.bandCount < minBandCount || block.bandCount >
            Tables.maxBandCount[block.config.sampleRateIndex()]) {
            return;
        }

        if (block.blockType == BlockType.STEREO) {
            block.stereoBand = reader.readInt(4);
            block.stereoBand += minBandCount;
            block.stereoQuantizationUnit = Tables.bandToQuantUnitCount[block.stereoBand];
        } else {
            block.stereoBand = block.bandCount;
        }

        block.bandExtensionEnabled = reader.readBool();
        if (block.bandExtensionEnabled) {
            block.extensionBand = reader.readInt(4);
            block.extensionBand += minBandCount;

            if (block.extensionBand < block.bandCount || block.extensionBand > maxExtensionBand) {
                throw new IllegalArgumentException();
            }

            block.extensionUnit = Tables.bandToQuantUnitCount[block.extensionBand];
        } else {
            block.extensionBand = block.bandCount;
            block.extensionUnit = block.quantizationUnitCount;
        }
    }

    private static void readGradientParams(BitReader reader, Block block) {
        block.gradientMode = reader.readInt(2);
        if (block.gradientMode > 0) {
            block.gradientEndUnit = 31;
            block.gradientEndValue = 31;
            block.gradientStartUnit = reader.readInt(5);
            block.gradientStartValue = reader.readInt(5);
        } else {
            block.gradientStartUnit = reader.readInt(6);
            block.gradientEndUnit = reader.readInt(6) + 1;
            block.gradientStartValue = reader.readInt(5);
            block.gradientEndValue = reader.readInt(5);
        }
        block.gradientBoundary = reader.readInt(4);
        if (block.gradientBoundary > block.quantizationUnitCount) {
            throw new IllegalArgumentException();
        }
        if (block.gradientStartUnit < 1 || block.gradientStartUnit >= 48) {
            throw new IllegalArgumentException();
        }
        if (block.gradientEndUnit < 1 || block.gradientEndUnit >= 48) {
            throw new IllegalArgumentException();
        }
        if (block.gradientStartUnit > block.gradientEndUnit) {
            throw new IllegalArgumentException();
        }
        if (block.gradientStartValue < 0 || block.gradientStartValue >= 32) {
            throw new IllegalArgumentException();
        }
        if (block.gradientEndValue < 0 || block.gradientEndValue >= 32) {
            throw new IllegalArgumentException();
        }
    }

    private static void readStereoParams(BitReader reader, Block block) {
        if (block.blockType != BlockType.STEREO) {
            return;
        }

        block.primaryChannelIndex = reader.readInt(1);
        block.hasJointStereoSigns = reader.readBool();
        if (block.hasJointStereoSigns) {
            for (int i = block.stereoQuantizationUnit; i < block.quantizationUnitCount; i++) {
                block.jointStereoSigns[i] = reader.readInt(1);
            }
        } else {
            Arrays.fill(block.jointStereoSigns, 0);
        }
    }

    private static void readExtensionParams(BitReader reader, Block block) {
        int bexBand = 0;
        if (block.bandExtensionEnabled) {
            int[] bexBand1 = new int[1];
            int[] unused1 = new int[1];
            int[] unused2 = new int[1];
            BandExtension.getBexBandInfo(bexBand1, unused1, unused2, block.quantizationUnitCount);
            bexBand = bexBand1[0];
            if (block.blockType == BlockType.STEREO) {
                readBexHeader(reader, block.channels[1], bexBand);
            } else {
                reader.position(reader.position() + 1);
            }
        }
        block.hasExtensionData = reader.readBool();

        if (!block.hasExtensionData) {
            return;
        }
        if (!block.bandExtensionEnabled) {
            block.bexMode = reader.readInt(2);
            block.bexDataLength = reader.readInt(5);
            reader.position(reader.position() + block.bexDataLength);
            return;
        }

        readBexHeader(reader, block.channels[0], bexBand);

        block.bexDataLength = reader.readInt(5);
        if (block.bexDataLength <= 0) {
            return;
        }
        int bexDataEnd = reader.position() + block.bexDataLength;

        readBexData(reader, block.channels[0], bexBand);

        if (block.blockType == BlockType.STEREO) {
            readBexData(reader, block.channels[1], bexBand);
        }

        // Make sure we didn't read too many bits
        if (reader.position() > bexDataEnd) {
            throw new IllegalArgumentException();
        }
    }

    private static void readBexHeader(BitReader reader, Channel channel, int bexBand) {
        int bexMode = reader.readInt(2);
        channel.bexMode = bexBand > 2 ? bexMode : 4;
        channel.bexValueCount = BandExtension.bexEncodedValueCounts[channel.bexMode][bexBand];
    }

    private static void readBexData(BitReader reader, Channel channel, int bexBand) {
        for (int i = 0; i < channel.bexValueCount; i++) {
            int dataLength = BandExtension.bexDataLengths[channel.bexMode][bexBand][i];
            channel.bexValues[i] = reader.readInt(dataLength);
        }
    }

    private static void calculateSpectrumCodebookIndex(Channel channel) {
        Arrays.fill(channel.CodebookSet, 0);
        int quantUnits = channel.codedQuantUnits;
        int[] sf = channel.scaleFactors;

        if (quantUnits <= 1) {
            return;
        }
        if (channel.config.highSampleRate()) {
            return;
        }

        // Temporarily setting this value allows for simpler code by
        // making the last value a non-special case.
        int originalScaleTmp = sf[quantUnits];
        sf[quantUnits] = sf[quantUnits - 1];

        int avg = 0;
        if (quantUnits > 12) {
            for (int i = 0; i < 12; i++) {
                avg += sf[i];
            }
            avg = (avg + 6) / 12;
        }

        for (int i = 8; i < quantUnits; i++) {
            int prevSf = sf[i - 1];
            int nextSf = sf[i + 1];
            int minSf = Math.min(prevSf, nextSf);
            if (sf[i] - minSf >= 3 || sf[i] - prevSf + sf[i] - nextSf >= 3) {
                channel.CodebookSet[i] = 1;
            }
        }

        for (int i = 12; i < quantUnits; i++) {
            if (channel.CodebookSet[i] == 0) {
                int minSf = Math.min(sf[i - 1], sf[i + 1]);
                if (sf[i] - minSf >= 2 && sf[i] >= avg - (Tables.quantUnitToCoeffCount[i] == 16 ? 1 : 0)) {
                    channel.CodebookSet[i] = 1;
                }
            }
        }

        sf[quantUnits] = originalScaleTmp;
    }

    private static void readSpectra(BitReader reader, Channel channel) {
        int[] values = channel.SpectraValuesBuffer;
        Arrays.fill(channel.QuantizedSpectra, 0);
        int maxHuffPrecision = Tables.maxHuffPrecision(channel.config.highSampleRate());

        for (int i = 0; i < channel.codedQuantUnits; i++) {
            int subbandCount = Tables.quantUnitToCoeffCount[i];
            int precision = channel.Precisions[i] + 1;
            if (precision <= maxHuffPrecision) {
                HuffmanCodebook huff = Tables.huffmanSpectrum[channel.CodebookSet[i]][precision][Tables.quantUnitToCodebookIndex[i]];
                int groupCount = subbandCount >>> huff.valueCountPower();
                for (int j = 0; j < groupCount; j++) {
                    values[j] = readHuffmanValue(huff, reader, false);
                }

                decodeHuffmanValues(channel.QuantizedSpectra, Tables.quantUnitToCoeffIndex[i], subbandCount, huff, values);
            } else {
                int subbandIndex = Tables.quantUnitToCoeffIndex[i];
                for (int j = subbandIndex; j < Tables.quantUnitToCoeffIndex[i + 1]; j++) {
                    channel.QuantizedSpectra[j] = reader.readSignedInt(precision);
                }
            }
        }
    }

    private static void readSpectraFine(BitReader reader, Channel channel) {
        Arrays.fill(channel.QuantizedSpectraFine, 0);

        for (int i = 0; i < channel.codedQuantUnits; i++) {
            if (channel.PrecisionsFine[i] > 0) {
                int overflowBits = channel.PrecisionsFine[i] + 1;
                int startSubband = Tables.quantUnitToCoeffIndex[i];
                int endSubband = Tables.quantUnitToCoeffIndex[i + 1];

                for (int j = startSubband; j < endSubband; j++) {
                    channel.QuantizedSpectraFine[j] = reader.readSignedInt(overflowBits);
                }
            }
        }
    }

    private static void decodeHuffmanValues(int[] spectrum, int index, int bandCount, HuffmanCodebook huff, int[] values) {
        int valueCount = bandCount >>> huff.valueCountPower();
        int mask = (1 << huff.valueBits()) - 1;

        for (int i = 0; i < valueCount; i++) {
            int value = values[i];
            for (int j = 0; j < huff.valueCount(); j++) {
                spectrum[index++] = Bit.signExtend32(value & mask, huff.valueBits());
                value >>= huff.valueBits();
            }
        }
    }

    public static int readHuffmanValue(HuffmanCodebook huff, BitReader reader, boolean signed/* = false*/) {
        int code = reader.peekInt(huff.maxBitSize());
        int value = Byte.toUnsignedInt(huff.lookup()[code]);
        int bits = Byte.toUnsignedInt(huff.bits()[value]);
        reader.position(reader.position() + bits);
        return signed ? Bit.signExtend32(value, huff.valueBits()) : value;
    }

    private static void unpackLfeBlock(BitReader reader, Block block) {
        Channel channel = block.channels[0];
        block.quantizationUnitCount = 2;

        decodeLfeScaleFactors(reader, channel);
        calculateLfePrecision(channel);
        channel.codedQuantUnits = block.quantizationUnitCount;
        readLfeSpectra(reader, channel);
    }

    private static void decodeLfeScaleFactors(BitReader reader, Channel channel) {
        Arrays.fill(channel.scaleFactors, 0);
        for (int i = 0; i < channel.block.quantizationUnitCount; i++) {
            channel.scaleFactors[i] = reader.readInt(5);
        }
    }

    private static void calculateLfePrecision(Channel channel) {
        Block block = channel.block;
        int precision = block.reuseBandParams ? 8 : 4;
        for (int i = 0; i < block.quantizationUnitCount; i++) {
            channel.Precisions[i] = precision;
            channel.PrecisionsFine[i] = 0;
        }
    }

    private static void readLfeSpectra(BitReader reader, Channel channel) {
        Arrays.fill(channel.QuantizedSpectra, 0);

        for (int i = 0; i < channel.codedQuantUnits; i++) {
            if (channel.Precisions[i] <= 0) {
                continue;
            }

            int precision = channel.Precisions[i] + 1;
            for (int j = Tables.quantUnitToCoeffIndex[i]; j < Tables.quantUnitToCoeffIndex[i + 1]; j++) {
                channel.QuantizedSpectra[j] = reader.readSignedInt(precision);
            }
        }
    }
}
