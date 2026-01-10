package sh.adelessfox.odradek.audio.codec;

import sh.adelessfox.atrac9j.Atrac9Decoder;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record AudioCodecAtrac9(byte[] configData, int encoderDelaySamples) implements AudioCodec {
    @Override
    public Audio toPcm16(AudioFormat format, byte[] data) {
        var decoder = Atrac9Decoder.of(configData);
        var config = decoder.config();

        int superframes = Math.ceilDiv(data.length, config.superframeBytes());
        int samples = superframes * config.superframeSamples() - encoderDelaySamples;

        var buffer = new short[config.channelCount()][config.superframeSamples()];
        var output = ByteBuffer
            .allocate(samples * config.channelCount() * Short.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < superframes; i++) {
            decoder.decode(data, i * config.superframeBytes(), config.superframeBytes(), buffer);

            // Skip encoder delay samples
            int smpl = i == 0 ? encoderDelaySamples : 0;

            for (; smpl < config.superframeSamples(); smpl++) {
                for (int chnl = 0; chnl < config.channelCount(); chnl++) {
                    output.putShort(buffer[chnl][smpl]);
                }
            }
        }

        var codec = new AudioCodecPcm(
            16, // LibAtrac9 outputs 16-bit PCM
            true,
            false
        );

        return new Audio(
            codec,
            format,
            samples,
            output.array()
        );
    }
}
