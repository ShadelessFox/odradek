package sh.adelessfox.odradek.audio.codec;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioFormat;

public record AudioCodecPcm16() implements AudioCodec {
    @Override
    public Audio toPcm16(AudioFormat format, int samples, byte[] data) {
        return new Audio(this, format, samples, data); // no-op
    }
}
