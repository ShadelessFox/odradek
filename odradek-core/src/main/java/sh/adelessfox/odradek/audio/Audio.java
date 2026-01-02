package sh.adelessfox.odradek.audio;

import sh.adelessfox.odradek.audio.codec.AudioCodec;

/**
 * Audio data container.
 *
 * @param codec  The audio codec
 * @param format The audio format
 * @param data   The codec-specific audio data
 */
public record Audio(AudioCodec codec, AudioFormat format, byte[] data) {
    public Audio toPcm16() {
        return codec.toPcm16(format, data);
    }
}
