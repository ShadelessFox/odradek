package sh.adelessfox.odradek.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.audio.container.riff.RiffParser;
import sh.adelessfox.odradek.audio.container.wave.WaveDataChunk;
import sh.adelessfox.odradek.audio.container.wave.WaveFmtChunk;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    // region vgmstream
    private static Audio convertPcm(Audio audio, AudioCodec.Pcm target) {
        try {
            return unpackWavToAudio(convertToWav(audio, target), target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert audio", e);
        }
    }

    private static byte[] convertToWav(Audio audio, AudioCodec.Pcm target) throws IOException, InterruptedException {
        var vgmstream = VgmstreamDownloader.download();
        var input = extractTempFile(audio);
        var command = buildVgmstreamCommand(vgmstream, input, target);

        try {
            var process = new ProcessBuilder()
                .command(command)
                .start();

            byte[] stdout;
            try (InputStream in = process.getInputStream()) {
                stdout = in.readAllBytes();
            }

            byte[] stderr;
            try (InputStream in = process.getErrorStream()) {
                stderr = in.readAllBytes();
            }

            int status = process.waitFor();
            if (status != 0) {
                log.error("vgmstream conversion failed with status {}, error: {}", status, new String(stderr, StandardCharsets.UTF_8));
                throw new IllegalStateException("vgmstream conversion failed with status " + status);
            }

            if (stderr.length > 0) {
                log.warn("vgmstream conversion produced warnings: {}", new String(stderr, StandardCharsets.UTF_8));
            }

            return stdout;
        } finally {
            try {
                Files.deleteIfExists(input);
            } catch (IOException e) {
                log.warn("Failed to delete temporary file: {}", input, e);
            }
        }
    }

    private static List<String> buildVgmstreamCommand(Path vgmstream, Path file, AudioCodec.Pcm target) {
        var format = switch (target) {
            case S16LE -> "-W1"; // PCM16
            case S24LE -> "-W2"; // PCM24
            case S32LE -> "-W3"; // PCM32
            case F32LE -> "-W4"; // float
        };
        return List.of(
            vgmstream.toAbsolutePath().toString(),
            file.toAbsolutePath().toString(),
            "-i", // ignore looping information
            "-p", // output to stdout
            "-P", // output to stdout even if stdout is a terminal
            format);
    }

    private static Audio unpackWavToAudio(byte[] wav, AudioCodec.Pcm target) throws IOException {
        var riff = new RiffParser()
            .reader(WaveFmtChunk.ID, WaveFmtChunk.reader())
            .reader(WaveDataChunk.ID, WaveDataChunk.reader())
            .parse(BinaryReader.wrap(wav));

        var fmt = riff.get(WaveFmtChunk.ID).orElseThrow(() -> new IllegalStateException("Missing fmt chunk"));
        var data = riff.get(WaveDataChunk.ID).orElseThrow(() -> new IllegalStateException("Missing data chunk"));

        int sampleRate = fmt.sampleRate();
        int channels = fmt.channelCount();
        int samples = data.data().length / (target.sizeBytes() * channels);

        return new Audio(target, new AudioFormat(sampleRate, channels), samples, data.data());
    }

    private static Path extractTempFile(Audio audio) {
        var suffix = switch (audio.codec()) {
            case AudioCodec.Atrac9 _ -> "input.at9";
            case AudioCodec.Wwise _ -> "input.wem";
            case AudioCodec.Pcm _ -> throw new IllegalArgumentException("PCM audio cannot be converted to PCM");
        };
        try {
            var file = Files.createTempFile("odradek-vgstream", suffix);
            Files.write(file, audio.data());
            return file;
        } catch (IOException e) {
            log.error("Failed to extract audio to temporary file", e);
            throw new UncheckedIOException(e);
        }
    }
    // endregion
}
