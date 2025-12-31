package sh.adelessfox.odradek.audio;

public record AudioFormat(AudioEncoding encoding, int sampleRate, int bitsPerSample, int channels) {
}
