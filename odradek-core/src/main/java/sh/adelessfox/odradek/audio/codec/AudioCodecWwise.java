package sh.adelessfox.odradek.audio.codec;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioFormat;
import sh.adelessfox.vgmstream.MemoryStreamFile;
import sh.adelessfox.vgmstream.libvgmstream.libvgmstream_config_t;
import sh.adelessfox.vgmstream.libvgmstream.libvgmstream_decoder_t;
import sh.adelessfox.vgmstream.libvgmstream.libvgmstream_format_t;
import sh.adelessfox.vgmstream.libvgmstream.libvgmstream_t;

import java.lang.foreign.Arena;

import static sh.adelessfox.vgmstream.libvgmstream.libvgmstream_h.*;

public record AudioCodecWwise() implements AudioCodec {
    private static final int BUFFER_SAMPLES = 4096;

    static {
        System.loadLibrary("ogg");
        System.loadLibrary("vorbis");
        System.loadLibrary("vorbisfile");
        System.loadLibrary("libatrac9");
        System.loadLibrary("libvgmstream");
    }

    @Override
    public Audio toPcm16(AudioFormat fmt, int samples, byte[] data) {
        var lib = libvgmstream_init();
        if (lib.address() == 0L) {
            throw new IllegalStateException("Failed to initialize libvgmstream");
        }

        try (var arena = Arena.ofConfined()) {
            var config = libvgmstream_config_t.allocate(arena);
            libvgmstream_config_t.ignore_loop(config, true);
            libvgmstream_config_t.force_sfmt(config, LIBVGMSTREAM_SFMT_PCM16());
            libvgmstream_config_t.stereo_track(config, 1);
            libvgmstream_setup(lib, config);
        }

        try (var arena = Arena.ofConfined()) {
            var file = MemoryStreamFile.allocate(arena, "buffer.wem", data);
            if (libvgmstream_open_stream(lib, file.allocate(arena), 0) < 0) {
                throw new IllegalStateException("Failed to open stream");
            }

            var format = libvgmstream_t.format(lib);
            int channels = libvgmstream_format_t.channels(format);
            int sampleRate = libvgmstream_format_t.sample_rate(format);

            var buffer = arena.allocate((long) BUFFER_SAMPLES * Short.BYTES * channels);
            var output = new byte[samples * Short.BYTES * channels];
            int offset = 0;

            var decoder = libvgmstream_t.decoder(lib);
            while (!libvgmstream_decoder_t.done(decoder)) {
                libvgmstream_fill(lib, buffer, BUFFER_SAMPLES);

                var buf = libvgmstream_decoder_t.buf(decoder);
                int len = libvgmstream_decoder_t.buf_bytes(decoder);
                buf.reinterpret(len).asByteBuffer().get(output, offset, len);
                offset += len;
            }

            libvgmstream_close_stream(lib);

            return new Audio(new AudioCodecPcm(16, true, false), new AudioFormat(sampleRate, channels), samples, output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            libvgmstream_free(lib);
        }
    }
}
