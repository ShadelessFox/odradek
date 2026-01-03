package sh.adelessfox.odradek.audio.codec;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioFormat;

public sealed interface AudioCodec
    permits AudioCodecAtrac9, AudioCodecPcm16 {

    Audio toPcm16(AudioFormat format, int samples, byte[] data);
}
