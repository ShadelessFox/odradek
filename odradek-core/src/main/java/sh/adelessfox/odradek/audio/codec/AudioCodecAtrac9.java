package sh.adelessfox.odradek.audio.codec;

import sh.adelessfox.atrac9j.Atrac9Decoder;
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

        try (var channel = Channels.newChannel(output)) {
            var decoder = Atrac9Decoder.of(configData);
            var config = decoder.config();

            var src = new byte[config.superframeBytes()];
            var dst = new short[config.channelCount()][config.superframeSamples()];
            var buf = ByteBuffer
                .allocate(config.superframeSamples() * config.channelCount() * Short.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);

            int superframes = Math.ceilDiv(data.length, config.superframeBytes());
            for (int i = 0; i < superframes; i++) {
                System.arraycopy(data, i * config.superframeBytes(), src, 0, config.superframeBytes());
                decoder.decode(src, dst);

                // TODO: Skip encoder delay frames

                buf.clear();
                for (int smpl = 0; smpl < config.superframeSamples(); smpl++) {
                    for (int chnl = 0; chnl < config.channelCount(); chnl++) {
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
