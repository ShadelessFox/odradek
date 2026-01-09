package sh.adelessfox.atrac9j;


import sh.adelessfox.atrac9j.util.BitReader;

/**
 * @param configData          Stores the configuration data needed to decode or encode an ATRAC9 stream.
 * @param sampleRateIndex     The 4-byte ATRAC9 configuration data.
 * @param channelConfigIndex  A 4-bit value specifying one of 16 sample rates.
 * @param frameBytes          A 3-bit value specifying one of 6 substream channel mappings.
 * @param superframeIndex     An 11-bit value containing the average size of a single frame.
 * @param channelConfig       A 2-bit value indicating how many frames are in each superframe.
 * @param channelCount        The channel mapping used by the ATRAC9 stream.
 * @param sampleRate          The total number of channels in the ATRAC9 stream.
 * @param highSampleRate      The sample rate of the ATRAC9 stream.
 * @param framesPerSuperframe Indicates whether the ATRAC9 stream has a {@link #sampleRateIndex} of 8 or above.
 * @param frameSamplesPower   The number of frames in each superframe.
 * @param frameSamples        The number of samples in one frame as an exponent of 2. {@link #frameSamples} = {@code 2^FrameSamplesPower}.
 * @param superframeBytes     The number of samples in one frame.
 * @param superframeSamples   The number of bytes in one superframe.
 */
public record Atrac9Config(
    byte[] configData,
    int sampleRateIndex,
    int channelConfigIndex,
    int frameBytes,
    int superframeIndex,
    ChannelConfig channelConfig,
    int channelCount,
    int sampleRate,
    boolean highSampleRate,
    int framesPerSuperframe,
    int frameSamplesPower,
    int frameSamples,
    int superframeBytes,
    int superframeSamples
) {
    /**
     * Reads ATRAC9 configuration data and calculates the stream parameters from it.
     *
     * @param configData The processed ATRAC9 configuration.
     */
    public static Atrac9Config read(byte[] configData) {
        if (configData == null || configData.length != 4) {
            throw new IllegalArgumentException("Config data must be 4 bytes long");
        }

        var reader = new BitReader(configData);
        var header = reader.readInt(8);
        var sampleRateIndex = reader.readInt(4);
        var channelConfigIndex = reader.readInt(3);
        var validationBit = reader.readInt(1);
        var frameBytes = reader.readInt(11) + 1;
        var superframeIndex = reader.readInt(2);

        if (header != 0xFE || validationBit != 0) {
            throw new IllegalArgumentException("ATRAC9 Config Data is invalid");
        }

        var framesPerSuperframe = 1 << superframeIndex;
        var superframeBytes = frameBytes << superframeIndex;
        var channelConfig = Tables.channelConfig[channelConfigIndex];

        var channelCount = channelConfig.channelCount();
        var sampleRate = Tables.sampleRates[sampleRateIndex];
        var highSampleRate = sampleRateIndex > 7;
        var frameSamplesPower = Tables.samplingRateIndexToFrameSamplesPower[sampleRateIndex];
        var frameSamples = 1 << frameSamplesPower;
        var superframeSamples = frameSamples * framesPerSuperframe;

        return new Atrac9Config(
            configData,
            sampleRateIndex,
            channelConfigIndex,
            frameBytes,
            superframeIndex,
            channelConfig,
            channelCount,
            sampleRate,
            highSampleRate,
            framesPerSuperframe,
            frameSamplesPower,
            frameSamples,
            superframeBytes,
            superframeSamples
        );
    }
}
