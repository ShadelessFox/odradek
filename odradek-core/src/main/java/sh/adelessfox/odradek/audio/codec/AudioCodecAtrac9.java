package sh.adelessfox.odradek.audio.codec;

import sh.adelessfox.libatrac9.Atrac9Decoder;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;

public record AudioCodecAtrac9(byte[] configData, int encoderDelay) implements AudioCodec {
    @Override
    public Audio toPcm16(AudioFormat format, int samples, byte[] data) {
        var output = new ByteArrayOutputStream();
        var decoder = new Atrac9Decoder();

        try (var channel = Channels.newChannel(output)) {
            decoder.Initialize(configData);

            var config = decoder.Config;
            int superframes = Math.ceilDiv(data.length, config.SuperframeBytes);

            var src = new byte[config.SuperframeBytes];
            var dst = new short[config.ChannelCount][config.SuperframeSamples];
            var buf = ByteBuffer
                .allocate(config.SuperframeSamples * config.ChannelCount * Short.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < superframes; i++) {
                System.arraycopy(data, i * config.SuperframeBytes, src, 0, config.SuperframeBytes);
                decoder.Decode(src, dst);

                // TODO: Skip encoder delay frames

                buf.clear();
                for (int smpl = 0; smpl < config.SuperframeSamples; smpl++) {
                    for (int chnl = 0; chnl < config.ChannelCount; chnl++) {
                        buf.putShort(dst[chnl][smpl]);
                    }
                }

                buf.flip();
                channel.write(buf);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var codec = new AudioCodecPcm(
            16,
            true,
            false
        );

        return new Audio(
            codec,
            format,
            samples,
            output.toByteArray()
        );
    }
}
