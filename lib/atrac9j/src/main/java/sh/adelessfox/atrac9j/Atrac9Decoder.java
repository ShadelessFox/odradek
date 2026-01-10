package sh.adelessfox.atrac9j;


import sh.adelessfox.atrac9j.util.BitReader;
import sh.adelessfox.atrac9j.util.Helpers;

/**
 * Decoder for ATRAC9 audio streams.
 */
public final class Atrac9Decoder {
    private final BitReader Reader = new BitReader();
    private final Atrac9Config config;
    private final Frame frame;

    private Atrac9Decoder(Atrac9Config config, Frame frame) {
        this.config = config;
        this.frame = frame;
    }

    /**
     * Sets up the decoder to decode an ATRAC9 stream based on the information in {@code configData}.
     *
     * @param configData A 4-byte value containing information about the ATRAC9 stream.
     */
    public static Atrac9Decoder of(byte[] configData) {
        var config = Atrac9Config.read(configData);
        var frame = Frame.of(config);
        return new Atrac9Decoder(config, frame);
    }

    /**
     * Decodes one superframe of ATRAC9 data into 16-bit PCM.
     *
     * @param atrac9Data The ATRAC9 data to decode. The array must be at least
     *                   {@link #config}.{@link Atrac9Config#superframeBytes()} bytes long.
     * @param pcmOut     A buffer that the decoded PCM data will be placed in.
     *                   The array must have dimensions of at least [{@link #config}.{@link Atrac9Config#channelCount()}]
     *                   [{@link #config}.{@link Atrac9Config#superframeSamples()}].
     */
    public void decode(byte[] atrac9Data, short[][] pcmOut) {
        if (config == null) {
            throw new IllegalStateException("Decoder must be initialized before decoding.");
        }

        validateDecodeBuffers(atrac9Data, pcmOut);
        Reader.setBuffer(atrac9Data);
        DecodeSuperFrame(pcmOut);
    }

    /**
     * The config data for the current ATRAC9 stream.
     */
    public Atrac9Config config() {
        return config;
    }

    private void validateDecodeBuffers(byte[] atrac9Buffer, short[][] pcmBuffer) {
        if (atrac9Buffer.length < config.superframeBytes()) {
            throw new IllegalArgumentException("ATRAC9 buffer is too small");
        }

        if (pcmBuffer.length < config.channelCount()) {
            throw new IllegalArgumentException("PCM buffer is too small");
        }

        for (int i = 0; i < config.channelCount(); i++) {
            if (pcmBuffer[i] != null && pcmBuffer[i].length < config.superframeSamples()) {
                throw new IllegalArgumentException("PCM buffer is too small");
            }
        }
    }

    private void DecodeSuperFrame(short[][] pcmOut) {
        for (int i = 0; i < config.framesPerSuperframe(); i++) {
            decodeFrame(Reader, frame, i);
            pcmFloatToShort(pcmOut, i * config.frameSamples());
            Reader.align(8);
        }
    }

    private void pcmFloatToShort(short[][] pcmOut, int start) {
        int endSample = start + config.frameSamples();
        int channelNum = 0;
        for (Block block : frame.blocks()) {
            for (Channel channel : block.channels) {
                double[] pcmSrc = channel.pcm;
                short[] pcmDest = pcmOut[channelNum++];
                for (int d = 0, s = start; s < endSample; d++, s++) {
                    double sample = pcmSrc[d];
                    // Not using Math.Round because it's ~20x slower on 64-bit
                    int roundedSample = (int) Math.floor(sample + 0.5);
                    pcmDest[s] = Helpers.clamp16(roundedSample);
                }
            }
        }
    }

    private static void decodeFrame(BitReader reader, Frame frame, int frameIndex) {
        Unpack.unpackFrame(reader, frame, frameIndex);

        for (Block block : frame.blocks()) {
            Quantization.dequantizeSpectra(block);
            Stereo.applyIntensityStereo(block);
            Quantization.scaleSpectrum(block);
            BandExtension.applyBandExtension(block);
            imdctBlock(block);
        }
    }

    private static void imdctBlock(Block block) {
        for (Channel channel : block.channels) {
            channel.mdct.runImdct(channel.spectra, channel.pcm);
        }
    }
}
