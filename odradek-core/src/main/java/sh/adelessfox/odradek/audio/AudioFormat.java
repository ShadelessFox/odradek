package sh.adelessfox.odradek.audio;

public record AudioFormat(
    int sampleCount,
    int sampleRate,
    int channelCount
) {
}
