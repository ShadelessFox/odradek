package sh.adelessfox.odradek.audio.codec;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioFormat;
import sh.adelessfox.odradek.audio.codec.libatrac9.Atrac9CodecInfo;
import sh.adelessfox.odradek.audio.codec.libatrac9.Atrac9Decoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.file.Path;

public record AudioCodecAtrac9(byte[] configData, int encoderDelay) implements AudioCodec {
    @Override
    public Audio toPcm16(AudioFormat format, int samples, byte[] data) {
        var baos = new ByteArrayOutputStream();

        try (
            var arena = Arena.ofConfined();
            var atrac9 = new Atrac9Decoder(arena, Path.of("D:/Tools/vgmstream-win64/libatrac9.dll"));
            var channel = Channels.newChannel(baos);
        ) {
            atrac9.initialize(configData);

            var info = atrac9.getCodecInfo();
            var src = arena.allocate(info.superframeSize()).asByteBuffer();
            var dst = arena.allocate(info.channels() * info.frameSamples() * info.framesInSuperframe() * (long) Short.BYTES).asByteBuffer();

            int superframeCount = Math.toIntExact(data.length / info.superframeSize());

            for (int i = 0; i < superframeCount; i++) {
                src.clear();
                src.clear().put(0, data, i * info.superframeSize(), info.superframeSize());

                dst.clear();
                decodeSuperFrame(atrac9, info, src, dst);

                dst.flip();

                if (i == 0) {
                    dst.position(dst.position() + encoderDelay * info.channels() * Short.BYTES);
                }

                channel.write(dst);
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
            baos.toByteArray()
        );
    }

    private static int decodeSuperFrame(Atrac9Decoder atrac9, Atrac9CodecInfo codec, ByteBuffer src, ByteBuffer dst) {
        var srcSegment = MemorySegment.ofBuffer(src);
        var dstSegment = MemorySegment.ofBuffer(dst);

        int srcPos = src.position();
        int dstPos = dst.position();
        int samples = 0;

        for (int i = 0; i < codec.framesInSuperframe(); i++) {
            int bytesRead = atrac9.decode(
                srcSegment.asSlice(srcPos),
                dstSegment.asSlice(dstPos));
            srcPos += bytesRead;
            dstPos += codec.frameSamples() * codec.channels() * Short.BYTES;
            samples += codec.frameSamples();
        }

        src.position(srcPos);
        dst.position(dstPos);

        return samples;
    }
}
