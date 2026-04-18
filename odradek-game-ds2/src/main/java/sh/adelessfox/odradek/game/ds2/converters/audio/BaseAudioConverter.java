package sh.adelessfox.odradek.game.ds2.converters.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioCodec;
import sh.adelessfox.odradek.audio.AudioFormat;
import sh.adelessfox.odradek.audio.container.riff.RiffFile;
import sh.adelessfox.odradek.audio.container.riff.RiffParser;
import sh.adelessfox.odradek.audio.container.wave.WaveDataChunk;
import sh.adelessfox.odradek.audio.container.wwise.WwiseFmtChunk;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

abstract class BaseAudioConverter<T> implements Converter<T, Audio, DS2Game> {
    private static final RiffParser RIFF_PARSER = new RiffParser()
        .type("WAVE")
        .reader(WwiseFmtChunk.ID, WwiseFmtChunk.reader())
        .reader(WaveDataChunk.ID, WaveDataChunk.reader());

    protected static Optional<Audio> convertWem(byte[] wem) {
        RiffFile file;

        try {
            file = RIFF_PARSER.parse(BinaryReader.wrap(wem));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var fmt = file.get(WwiseFmtChunk.ID).orElseThrow();
        var data = file.get(WaveDataChunk.ID).orElseThrow();

        var codec = new AudioCodec.Wwise();
        var format = new AudioFormat(fmt.sampleRate(), fmt.channelCount());
        var sampleCount = fmt.sampleCount().orElseGet(() -> pcmSamples(data, fmt));

        return Optional.of(new Audio(codec, format, sampleCount, wem));
    }

    private static int pcmSamples(WaveDataChunk data, WwiseFmtChunk fmt) {
        return BaseAudioConverter.pcmBytesToSamples(
            data.data().length,
            fmt.channelCount(),
            fmt.bitsPerSample());
    }

    private static int pcmBytesToSamples(int byteCount, int channelCount, int bitsPerSample) {
        return byteCount / (channelCount * bitsPerSample / 8);
    }
}
