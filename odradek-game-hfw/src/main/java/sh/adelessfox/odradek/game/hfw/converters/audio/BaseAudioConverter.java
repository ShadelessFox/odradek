package sh.adelessfox.odradek.game.hfw.converters.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioCodec;
import sh.adelessfox.odradek.audio.AudioFormat;
import sh.adelessfox.odradek.audio.container.at9.Atrac9FactChunk;
import sh.adelessfox.odradek.audio.container.at9.Atrac9FmtChunk;
import sh.adelessfox.odradek.audio.container.riff.RiffFile;
import sh.adelessfox.odradek.audio.container.riff.RiffParser;
import sh.adelessfox.odradek.audio.container.wave.WaveDataChunk;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

abstract class BaseAudioConverter<T> implements Converter<T, Audio, HFWGame> {
    private static final RiffParser RIFF_PARSER = new RiffParser()
        .type("WAVE")
        .reader(Atrac9FmtChunk.ID, Atrac9FmtChunk.reader())
        .reader(Atrac9FactChunk.ID, Atrac9FactChunk.reader())
        .reader(WaveDataChunk.ID, WaveDataChunk.reader());

    protected static Optional<Audio> convertPcm(
        int bitsPerSample,
        int sampleRate,
        int channelCount,
        int sampleCount,
        byte[] stream
    ) {
        var codec = switch (bitsPerSample) {
            case 16 -> AudioCodec.Pcm.S16LE;
            case 24 -> AudioCodec.Pcm.S24LE;
            case 32 -> AudioCodec.Pcm.S32LE;
            default -> throw new IllegalArgumentException("unsupported bitsPerSample: " + bitsPerSample);
        };
        var format = new AudioFormat(sampleRate, channelCount);
        return Optional.of(new Audio(codec, format, sampleCount, stream));
    }

    protected static Optional<Audio> convertAtrac9(byte[] stream) {
        RiffFile file;

        try {
            file = RIFF_PARSER.parse(BinaryReader.wrap(stream));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var fmt = file.get(Atrac9FmtChunk.ID).orElseThrow();
        var fact = file.get(Atrac9FactChunk.ID).orElseThrow();

        var codec = new AudioCodec.Atrac9();
        var format = new AudioFormat(fmt.sampleRate(), fmt.channelCount());
        return Optional.of(new Audio(codec, format, fact.sampleCount(), stream));
    }
}
