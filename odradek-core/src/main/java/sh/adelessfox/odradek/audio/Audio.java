package sh.adelessfox.odradek.audio;

/**
 * Audio data container.
 *
 * @param codec   The audio codec
 * @param format  The audio format
 * @param samples The number of samples
 * @param data    The codec-specific audio data
 */
public record Audio(AudioCodec codec, AudioFormat format, int samples, byte[] data) {
    public Audio {
        if (samples <= 0) {
            throw new IllegalArgumentException("samples must be positive");
        }
    }

    public Audio convert(AudioCodec codec, AudioFormat format) {
        return AudioConverter.convert(this, codec, format);
    }

    public Audio convert(AudioCodec codec, int channels) {
        return AudioConverter.convert(this, codec, new AudioFormat(format.sampleRate(), channels));
    }

    public Audio convert(AudioCodec codec) {
        return convert(codec, format);
    }
}
