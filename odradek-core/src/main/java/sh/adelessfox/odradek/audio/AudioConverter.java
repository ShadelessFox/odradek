package sh.adelessfox.odradek.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.vgmstream.libvgmstream.libvgmstream_config_t;
import sh.adelessfox.vgmstream.libvgmstream.libvgmstream_decoder_t;
import sh.adelessfox.vgmstream.libvgmstream.libvgmstream_format_t;
import sh.adelessfox.vgmstream.libvgmstream.libvgmstream_t;
import sh.adelessfox.vgmstream.util.MemoryStreamFile;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static sh.adelessfox.vgmstream.libvgmstream.libvgmstream_h.*;

final class AudioConverter {
    private static final Logger log = LoggerFactory.getLogger(AudioConverter.class);

    private AudioConverter() {
    }

    static Audio convert(Audio audio, AudioCodec codec, AudioFormat format) {
        Audio converted = audio;
        converted = transcode(converted, codec);
        converted = remix(converted, (AudioCodec.Pcm) codec, format);
        return converted;
    }

    private static Audio transcode(Audio audio, AudioCodec codec) {
        if (audio.codec().equals(codec)) {
            return audio;
        }
        if (!(codec instanceof AudioCodec.Pcm pcm)) {
            // Only PCM conversion is supported
            throw new IllegalArgumentException("Unsupported codec: " + codec);
        }
        return convertPcm(audio, pcm);
    }

    private static Audio remix(Audio audio, AudioCodec.Pcm codec, AudioFormat format) {
        if (audio.format().equals(format)) {
            return audio;
        }

        int sourceChannels = audio.format().channels();
        int targetChannels = format.channels();
        if (sourceChannels != targetChannels) {
            var buffer = remap(audio.data(), audio.samples(), codec.sizeBytes(), sourceChannels, targetChannels);
            return new Audio(codec, format, audio.samples(), buffer);
        }

        return new Audio(codec, format, audio.samples(), audio.data());
    }

    private static byte[] remap(
        byte[] data,
        int sampleCount,
        int sampleSize,
        int sourceChannels,
        int targetChannels
    ) {
        var buffer = new byte[sampleCount * sampleSize * targetChannels];
        for (int i = 0; i < sampleCount; i++) {
            for (int ch = 0; ch < targetChannels; ch++) {
                int srcCh = ch % sourceChannels;
                System.arraycopy(
                    data, (i * sourceChannels + srcCh) * sampleSize,
                    buffer, (i * targetChannels + ch) * sampleSize,
                    sampleSize);
            }
        }
        return buffer;
    }

    private static Audio convertPcm(Audio audio, AudioCodec.Pcm target) {
        var lib = libvgmstream_init();
        if (lib.address() == 0L) {
            throw new IllegalStateException("Failed to initialize libvgmstream");
        }

        try (var arena = Arena.ofConfined()) {
            var config = libvgmstream_config_t.allocate(arena);
            libvgmstream_config_t.ignore_loop(config, true);
            libvgmstream_config_t.force_sfmt(
                config, switch (target) {
                    case S16LE -> LIBVGMSTREAM_SFMT_PCM16();
                    case S24LE -> LIBVGMSTREAM_SFMT_PCM24();
                    case S32LE -> LIBVGMSTREAM_SFMT_PCM32();
                    case F32LE -> LIBVGMSTREAM_SFMT_FLOAT();
                });
            libvgmstream_setup(lib, config);

            var filename = switch (audio.codec()) {
                case AudioCodec.Atrac9 _ -> "input.at9";
                case AudioCodec.Wwise _ -> "input.wem";
                case AudioCodec.Pcm _ -> throw new IllegalArgumentException("PCM audio cannot be converted to PCM");
            };

            var stream = MemoryStreamFile.allocate(arena, filename, audio.data());
            if (libvgmstream_open_stream(lib, stream.allocate(arena), 0) < 0) {
                throw new IllegalStateException("Failed to open stream");
            }

            var description = arena.allocate(1024);
            libvgmstream_format_describe(lib, description, (int) description.byteSize());
            description.getString(0).lines().forEach(line -> log.debug("libvgmstream: {}", line));

            var format = libvgmstream_t.format(lib);
            int channels = libvgmstream_format_t.channels(format);
            var sampleRate = libvgmstream_format_t.sample_rate(format);

            var buffer = arena.allocate(1024L * target.sizeBytes() * channels, 16);
            var output = new byte[audio.samples() * target.sizeBytes() * channels];
            int offset = 0;

            var decoder = libvgmstream_t.decoder(lib);
            while (!libvgmstream_decoder_t.done(decoder)) {
                libvgmstream_fill(lib, buffer, 1024);

                var buf = libvgmstream_decoder_t.buf(decoder);
                int len = libvgmstream_decoder_t.buf_bytes(decoder);
                MemorySegment.copy(buf, ValueLayout.JAVA_BYTE, 0, output, offset, len);
                offset += len;
            }

            libvgmstream_close_stream(lib);

            return new Audio(target, new AudioFormat(sampleRate, channels), audio.samples(), output);
        } finally {
            libvgmstream_free(lib);
        }
    }
}
