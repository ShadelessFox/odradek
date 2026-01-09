package sh.adelessfox.atrac9j;


import java.util.Arrays;

final class BitAllocation {
    private BitAllocation() {
    }

    static void createGradient(Block block) {
        int valueCount = block.gradientEndValue - block.gradientStartValue;
        int unitCount = block.gradientEndUnit - block.gradientStartUnit;

        for (int i = 0; i < block.gradientEndUnit; i++) {
            block.gradient[i] = block.gradientStartValue;
        }

        for (int i = block.gradientEndUnit; i <= block.quantizationUnitCount; i++) {
            block.gradient[i] = block.gradientEndValue;
        }
        if (unitCount <= 0) {
            return;
        }
        if (valueCount == 0) {
            return;
        }

        byte[] curve = Tables.gradientCurves[unitCount - 1];
        if (valueCount <= 0) {
            double scale = (-valueCount - 1) / 31.0;
            int baseVal = block.gradientStartValue - 1;
            for (int i = block.gradientStartUnit; i < block.gradientEndUnit; i++) {
                block.gradient[i] = baseVal - (int) (curve[i - block.gradientStartUnit] * scale);
            }
        } else {
            double scale = (valueCount - 1) / 31.0;
            int baseVal = block.gradientStartValue + 1;
            for (int i = block.gradientStartUnit; i < block.gradientEndUnit; i++) {
                block.gradient[i] = baseVal + (int) (curve[i - block.gradientStartUnit] * scale);
            }
        }
    }

    static void calculateMask(Channel channel) {
        Arrays.fill(channel.PrecisionMask, 0);
        for (int i = 1; i < channel.block.quantizationUnitCount; i++) {
            int delta = channel.scaleFactors[i] - channel.scaleFactors[i - 1];
            if (delta > 1) {
                channel.PrecisionMask[i] += Math.min(delta - 1, 5);
            } else if (delta < -1) {
                channel.PrecisionMask[i - 1] += Math.min(delta * -1 - 1, 5);
            }
        }
    }

    static void calculatePrecisions(Channel channel) {
        Block block = channel.block;

        if (block.gradientMode != 0) {
            for (int i = 0; i < block.quantizationUnitCount; i++) {
                channel.Precisions[i] = channel.scaleFactors[i] + channel.PrecisionMask[i] - block.gradient[i];
                if (channel.Precisions[i] > 0) {
                    switch (block.gradientMode) {
                        case 1:
                            channel.Precisions[i] /= 2;
                            break;
                        case 2:
                            channel.Precisions[i] = 3 * channel.Precisions[i] / 8;
                            break;
                        case 3:
                            channel.Precisions[i] /= 4;
                            break;
                    }
                }
            }
        } else {
            for (int i = 0; i < block.quantizationUnitCount; i++) {
                channel.Precisions[i] = channel.scaleFactors[i] - block.gradient[i];
            }
        }

        for (int i = 0; i < block.quantizationUnitCount; i++) {
            if (channel.Precisions[i] < 1) {
                channel.Precisions[i] = 1;
            }
        }

        for (int i = 0; i < block.gradientBoundary; i++) {
            channel.Precisions[i]++;
        }

        for (int i = 0; i < block.quantizationUnitCount; i++) {
            channel.PrecisionsFine[i] = 0;
            if (channel.Precisions[i] > 15) {
                channel.PrecisionsFine[i] = channel.Precisions[i] - 15;
                channel.Precisions[i] = 15;
            }
        }
    }

    static byte[][] generateGradientCurves() {
        byte[] main = {
            1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15,
            16, 18, 19, 20, 21, 22, 23, 24, 25, 26, 26, 27, 27, 28, 28, 28, 29, 29, 29, 29, 30, 30, 30, 30
        };
        var curves = new byte[main.length][];

        for (int length = 1; length <= main.length; length++) {
            curves[length - 1] = new byte[length];
            for (int i = 0; i < length; i++) {
                curves[length - 1][i] = main[i * main.length / length];
            }
        }
        return curves;
    }
}
