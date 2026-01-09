package sh.adelessfox.atrac9j;


import sh.adelessfox.atrac9j.util.BitReader;

/// <summary>
/// Stores the configuration data needed to decode or encode an ATRAC9 stream.
/// </summary>
public final class Atrac9Config {
    /// <summary>
    /// The 4-byte ATRAC9 configuration data.
    /// </summary>
    public final byte[] ConfigData;

    /// <summary>
    /// A 4-bit value specifying one of 16 sample rates.
    /// </summary>
    public final int SampleRateIndex;
    /// <summary>
    /// A 3-bit value specifying one of 6 substream channel mappings.
    /// </summary>
    public final int ChannelConfigIndex;
    /// <summary>
    /// An 11-bit value containing the average size of a single frame.
    /// </summary>
    public final int FrameBytes;
    /// <summary>
    /// A 2-bit value indicating how many frames are in each superframe.
    /// </summary>
    public final int SuperframeIndex;

    /// <summary>
    /// The channel mapping used by the ATRAC9 stream.
    /// </summary>
    public final ChannelConfig ChannelConfig;
    /// <summary>
    /// The total number of channels in the ATRAC9 stream.
    /// </summary>
    public final int ChannelCount;
    /// <summary>
    /// The sample rate of the ATRAC9 stream.
    /// </summary>
    public final int SampleRate;
    /// <summary>
    /// Indicates whether the ATRAC9 stream has a <see cref="SampleRateIndex"/> of 8 or above.
    /// </summary>
    public final boolean HighSampleRate;

    /// <summary>
    /// The number of frames in each superframe.
    /// </summary>
    public final int FramesPerSuperframe;
    /// <summary>
    /// The number of samples in one frame as an exponent of 2.
    /// <see cref="FrameSamples"/> = 2^<see cref="FrameSamplesPower"/>.
    /// </summary>
    public final int FrameSamplesPower;
    /// <summary>
    /// The number of samples in one frame.
    /// </summary>
    public final int FrameSamples;
    /// <summary>
    /// The number of bytes in one superframe.
    /// </summary>
    public final int SuperframeBytes;
    /// <summary>
    /// The number of samples in one superframe.
    /// </summary>
    public final int SuperframeSamples;

    /// <summary>
    /// Reads ATRAC9 configuration data and calculates the stream parameters from it.
    /// </summary>
    /// <param name="configData">The processed ATRAC9 configuration.</param>
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
