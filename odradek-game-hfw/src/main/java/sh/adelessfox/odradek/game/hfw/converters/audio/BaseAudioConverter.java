package sh.adelessfox.odradek.game.hfw.converters.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioFormat;
import sh.adelessfox.odradek.audio.codec.AudioCodecAtrac9;
import sh.adelessfox.odradek.audio.codec.AudioCodecPcm;
import sh.adelessfox.odradek.audio.container.at9.Atrac9FactChunk;
import sh.adelessfox.odradek.audio.container.at9.Atrac9FmtChunk;
import sh.adelessfox.odradek.audio.container.riff.RiffFile;
import sh.adelessfox.odradek.audio.container.riff.RiffParser;
import sh.adelessfox.odradek.audio.container.wave.WaveDataChunk;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

abstract class BaseAudioConverter<T> implements Converter<T, Audio, ForbiddenWestGame> {
    private static final RiffParser RIFF_PARSER = new RiffParser()
        .type("WAVE")
        .reader(Atrac9FmtChunk.ID, Atrac9FmtChunk.reader())
        .reader(Atrac9FactChunk.ID, Atrac9FactChunk.reader())
        .reader(WaveDataChunk.ID, WaveDataChunk.reader());

    protected static Optional<Audio> convertPcm(int bitsPerSample, int sampleRate, int channelCount, int sampleCount, byte[] stream) {
        var codec = new AudioCodecPcm(bitsPerSample, true, false);
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
        var data = file.get(WaveDataChunk.ID).orElseThrow();

        var codec = new AudioCodecAtrac9(fmt.extension().configData(), fact.encoderDelaySamples());
        var format = new AudioFormat(fmt.sampleRate(), fmt.channelCount());
        return Optional.of(new Audio(codec, format, fact.sampleCount(), data.data()));
    }
}
