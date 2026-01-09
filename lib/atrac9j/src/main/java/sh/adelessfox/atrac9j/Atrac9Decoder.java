package sh.adelessfox.atrac9j;


import sh.adelessfox.atrac9j.util.BitReader;
import sh.adelessfox.atrac9j.util.Helpers;

/**
 * Decodes an ATRAC9 stream into 16-bit PCM.
 */
public final class Atrac9Decoder {
    /**
     * The config data for the current ATRAC9 stream.
     */
    public Atrac9Config Config;
    private final BitReader Reader = new BitReader();
    private Frame Frame;

    /**
     * Sets up the decoder to decode an ATRAC9 stream based on the information in {@code configData}.
     *
     * @param configData A 4-byte value containing information about the ATRAC9 stream.
     */
    public void Initialize(byte[] configData) {
        Config = new Atrac9Config(configData);
        Frame = new Frame(Config);
    }

    /**
     * Decodes one superframe of ATRAC9 data.
     *
     * @param atrac9Data The ATRAC9 data to decode. The array must be at least
     *                   {@link #Config}.{@link Atrac9Config#SuperframeBytes} bytes long.
     * @param pcmOut     A buffer that the decoded PCM data will be placed in.
     *                   The array must have dimensions of at least [{@link #Config}.{@link Atrac9Config#ChannelCount}]
     *                   [{@link #Config}.{@link Atrac9Config#SuperframeSamples}].
     */
    public void Decode(byte[] atrac9Data, short[][] pcmOut) {
        if (Config == null) {
            throw new IllegalStateException("Decoder must be initialized before decoding.");
        }

        ValidateDecodeBuffers(atrac9Data, pcmOut);
        Reader.SetBuffer(atrac9Data);
        DecodeSuperFrame(pcmOut);
    }

    private void ValidateDecodeBuffers(byte[] atrac9Buffer, short[][] pcmBuffer) {
        if (atrac9Buffer == null) {
            throw new NullPointerException("atrac9Buffer");
        }
        if (pcmBuffer == null) {
            throw new NullPointerException("pcmBuffer");
        }

        if (atrac9Buffer.length < Config.SuperframeBytes) {
            throw new IllegalArgumentException("ATRAC9 buffer is too small");
        }

        if (pcmBuffer.length < Config.ChannelCount) {
            throw new IllegalArgumentException("PCM buffer is too small");
        }

        for (int i = 0; i < Config.ChannelCount; i++) {
            if (pcmBuffer[i] != null && pcmBuffer[i].length < Config.SuperframeSamples) {
                throw new IllegalArgumentException("PCM buffer is too small");
            }
        }
    }

    private void DecodeSuperFrame(short[][] pcmOut) {
        for (int i = 0; i < Config.FramesPerSuperframe; i++) {
            Frame.FrameIndex = i;
            DecodeFrame(Reader, Frame);
            PcmFloatToShort(pcmOut, i * Config.FrameSamples);
            Reader.AlignPosition(8);
        }
    }

    private void PcmFloatToShort(short[][] pcmOut, int start) {
        int endSample = start + Config.FrameSamples;
        int channelNum = 0;
        for (Block block : Frame.Blocks) {
            for (Channel channel : block.Channels) {
                double[] pcmSrc = channel.Pcm;
                short[] pcmDest = pcmOut[channelNum++];
                for (int d = 0, s = start; s < endSample; d++, s++) {
                    double sample = pcmSrc[d];
                    // Not using Math.Round because it's ~20x slower on 64-bit
                    int roundedSample = (int) Math.floor(sample + 0.5);
                    pcmDest[s] = Helpers.Clamp16(roundedSample);
                }
            }
        }
    }

    private static void DecodeFrame(BitReader reader, Frame frame) {
        Unpack.UnpackFrame(reader, frame);

        for (Block block : frame.Blocks) {
            Quantization.DequantizeSpectra(block);
            Stereo.ApplyIntensityStereo(block);
            Quantization.ScaleSpectrum(block);
            BandExtension.ApplyBandExtension(block);
            ImdctBlock(block);
        }
    }

    private static void ImdctBlock(Block block) {
        for (Channel channel : block.Channels) {
            channel.Mdct.RunImdct(channel.Spectra, channel.Pcm);
        }
    }
}
