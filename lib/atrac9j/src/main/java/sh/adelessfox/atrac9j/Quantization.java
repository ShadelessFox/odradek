package sh.adelessfox.atrac9j;


import java.util.Arrays;

final class Quantization {
    private Quantization() {
    }

    static void dequantizeSpectra(Block block) {
        for (Channel channel : block.channels) {
            Arrays.fill(channel.spectra, 0);

            for (int i = 0; i < channel.codedQuantUnits; i++) {
                dequantizeQuantUnit(channel, i);
            }
        }
    }

    private static void dequantizeQuantUnit(Channel channel, int band) {
        int subBandIndex = Tables.quantUnitToCoeffIndex[band];
        int subBandCount = Tables.quantUnitToCoeffCount[band];
        double stepSize = Tables.quantizerStepSize[channel.Precisions[band]];
        double stepSizeFine = Tables.quantizerFineStepSize[channel.PrecisionsFine[band]];

        for (int sb = 0; sb < subBandCount; sb++) {
            double coarse = channel.QuantizedSpectra[subBandIndex + sb] * stepSize;
            double fine = channel.QuantizedSpectraFine[subBandIndex + sb] * stepSizeFine;
            channel.spectra[subBandIndex + sb] = coarse + fine;
        }
    }

    static void scaleSpectrum(Block block) {
        for (Channel channel : block.channels) {
            scaleSpectrum(channel);
        }
    }

    private static void scaleSpectrum(Channel channel) {
        int quantUnitCount = channel.block.quantizationUnitCount;
        double[] spectra = channel.spectra;

        for (int i = 0; i < quantUnitCount; i++) {
            for (int sb = Tables.quantUnitToCoeffIndex[i]; sb < Tables.quantUnitToCoeffIndex[i + 1]; sb++) {
                spectra[sb] *= Tables.spectrumScale[channel.scaleFactors[i]];
            }
        }
    }
}

