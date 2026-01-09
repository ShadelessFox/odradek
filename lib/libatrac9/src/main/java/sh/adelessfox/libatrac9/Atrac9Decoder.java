package sh.adelessfox.libatrac9;


import sh.adelessfox.libatrac9.util.BitReader;
import sh.adelessfox.libatrac9.util.Helpers;

/// <summary>
/// Decodes an ATRAC9 stream into 16-bit PCM.
/// </summary>
public final class Atrac9Decoder {
    /// <summary>
    /// The config data for the current ATRAC9 stream.
    /// </summary>
    public Atrac9Config Config;

    private Frame Frame;
    private BitReader Reader;
    private boolean _initialized;

    /// <summary>
    /// Sets up the decoder to decode an ATRAC9 stream based on the information in <paramref name="configData"/>.
    /// </summary>
    /// <param name="configData">A 4-byte value containing information about the ATRAC9 stream.</param>
    public void Initialize(byte[] configData) {
        Config = new Atrac9Config(configData);
        Frame = new Frame(Config);
        Reader = new BitReader(null);
        _initialized = true;
    }

    /// <summary>
    /// Decodes one superframe of ATRAC9 data.
    /// </summary>
    /// <param name="atrac9Data">The ATRAC9 data to decode. The array must be at least
    /// <see cref="Config"/>.<see cref="Atrac9Config.SuperframeBytes"/> bytes long.</param>
    /// <param name="pcmOut">A buffer that the decoded PCM data will be placed in.
    /// The array must have dimensions of at least [<see cref="Config"/>.<see cref="Atrac9Config.ChannelCount"/>]
    /// [<see cref="Config"/>.<see cref="Atrac9Config.SuperframeSamples"/>].</param>
    public void Decode(byte[] atrac9Data, short[][] pcmOut) {
        if (!_initialized) {
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
