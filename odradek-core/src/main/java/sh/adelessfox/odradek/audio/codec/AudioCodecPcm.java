package sh.adelessfox.odradek.audio.codec;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioFormat;

public record AudioCodecPcm(int bits, boolean signed, boolean bigEndian) implements AudioCodec {
    public AudioCodecPcm {
        if (bits != 8 && bits != 16 && bits != 32) {
            throw new IllegalArgumentException("unsupported PCM bits: " + bits);
        }
    }

    @Override
    public Audio toPcm16(AudioFormat format, byte[] data) {
        if (bits != 16) {
            throw new UnsupportedOperationException();
        }
        return new Audio(this, format, data.length / format.channels() / Short.BYTES, data); // no-op
    }
}
