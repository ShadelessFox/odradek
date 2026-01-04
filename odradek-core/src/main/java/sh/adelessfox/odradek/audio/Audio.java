package sh.adelessfox.odradek.audio;

import sh.adelessfox.odradek.audio.codec.AudioCodec;

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

    public Audio toPcm16() {
        return codec.toPcm16(format, samples, data);
    }
}
