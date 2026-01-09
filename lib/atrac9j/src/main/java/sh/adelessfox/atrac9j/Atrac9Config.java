package sh.adelessfox.atrac9j;


import sh.adelessfox.atrac9j.util.BitReader;

/**
 * Stores the configuration data needed to decode or encode an ATRAC9 stream.
 */
public final class Atrac9Config {
    /**
     * The 4-byte ATRAC9 configuration data.
     */
    public final byte[] ConfigData;

    /**
     * A 4-bit value specifying one of 16 sample rates.
     */
    public final int SampleRateIndex;
    /**
     * A 3-bit value specifying one of 6 substream channel mappings.
     */
    public final int ChannelConfigIndex;
    /**
     * An 11-bit value containing the average size of a single frame.
     */
    public final int FrameBytes;
    /**
     * A 2-bit value indicating how many frames are in each superframe.
     */
    public final int SuperframeIndex;

    /**
     * The channel mapping used by the ATRAC9 stream.
     */
    public final ChannelConfig ChannelConfig;
    /**
     * The total number of channels in the ATRAC9 stream.
     */
    public final int ChannelCount;
    /**
     * The sample rate of the ATRAC9 stream.
     */
    public final int SampleRate;
    /**
     * Indicates whether the ATRAC9 stream has a {@link #SampleRateIndex} of 8 or above.
     */
    public final boolean HighSampleRate;

    /**
     * The number of frames in each superframe.
     */
    public final int FramesPerSuperframe;
    /**
     * The number of samples in one frame as an exponent of 2.
     * {@link #FrameSamples} = {@code 2^FrameSamplesPower}.
     */
    public final int FrameSamplesPower;
    /**
     * The number of samples in one frame.
     */
    public final int FrameSamples;
    /**
     * The number of bytes in one superframe.
     */
    public final int SuperframeBytes;
    /**
     * The number of samples in one superframe.
     */
    public final int SuperframeSamples;

    /**
     * Reads ATRAC9 configuration data and calculates the stream parameters from it.
     *
     * @param configData The processed ATRAC9 configuration.
     */
    public Atrac9Config(byte[] configData) {
        if (configData == null || configData.length != 4) {
            throw new IllegalArgumentException("Config data must be 4 bytes long");
        }

        int[] a = new int[1];
        int[] b = new int[1];
        int[] c = new int[1];
        int[] d = new int[1];
        ReadConfigData(configData, a, b, c, d);
        SampleRateIndex = a[0];
        ChannelConfigIndex = b[0];
        FrameBytes = c[0];
        SuperframeIndex = d[0];
        ConfigData = configData;

        FramesPerSuperframe = 1 << SuperframeIndex;
        SuperframeBytes = FrameBytes << SuperframeIndex;
        ChannelConfig = Tables.ChannelConfig[ChannelConfigIndex];

        ChannelCount = ChannelConfig.ChannelCount;
        SampleRate = Tables.SampleRates[SampleRateIndex];
        HighSampleRate = SampleRateIndex > 7;
        FrameSamplesPower = Tables.SamplingRateIndexToFrameSamplesPower[SampleRateIndex];
        FrameSamples = 1 << FrameSamplesPower;
        SuperframeSamples = FrameSamples * FramesPerSuperframe;
    }

    private static void ReadConfigData(byte[] configData, int[] sampleRateIndex, int[] channelConfigIndex, int[] frameBytes, int[] superframeIndex) {
        var reader = new BitReader(configData);

        int header = reader.ReadInt(8);
        sampleRateIndex[0] = reader.ReadInt(4);
        channelConfigIndex[0] = reader.ReadInt(3);
        int validationBit = reader.ReadInt(1);
        frameBytes[0] = reader.ReadInt(11) + 1;
        superframeIndex[0] = reader.ReadInt(2);

        if (header != 0xFE || validationBit != 0) {
            throw new IllegalArgumentException("ATRAC9 Config Data is invalid");
        }
    }
}
