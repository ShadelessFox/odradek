package sh.adelessfox.odradek.audio;

public record AudioFormat(int sampleRate, int channels) {
    public AudioFormat {
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("sampleRate must be positive");
        }
        if (channels != 1 && channels != 2) {
            throw new IllegalArgumentException("channels must be 1 (mono) or 2 (stereo)");
        }
    }
}
