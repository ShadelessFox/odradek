package sh.adelessfox.odradek.audio.libatrac9;

public record Atrac9CodecInfo(
    int channels,
    int channelConfigIndex,
    int samplingRate,
    int superframeSize,
    int framesInSuperframe,
    int frameSamples,
    int wlength,
    int configData
) {
    static final int BYTES = 32;
}
