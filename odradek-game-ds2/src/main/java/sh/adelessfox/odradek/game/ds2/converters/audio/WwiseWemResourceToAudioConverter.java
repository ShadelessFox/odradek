package sh.adelessfox.odradek.game.ds2.converters.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioCodec;
import sh.adelessfox.odradek.audio.AudioFormat;
import sh.adelessfox.odradek.audio.container.riff.RiffFile;
import sh.adelessfox.odradek.audio.container.riff.RiffParser;
import sh.adelessfox.odradek.audio.container.wwise.WwiseFmtChunk;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.WwiseWemLocalizedResource;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.WwiseWemResource;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

public final class WwiseWemResourceToAudioConverter implements Converter<WwiseWemResource, Audio, DS2Game> {
    private static final RiffParser RIFF_PARSER = new RiffParser()
        .type("WAVE")
        .reader(WwiseFmtChunk.ID, WwiseFmtChunk.reader());

    @Override
    public Optional<Audio> convert(WwiseWemResource object, DS2Game game) {
        return switch (object) {
            case WwiseWemLocalizedResource o -> convertWwiseWemLocalizedResource(o, game);
            case WwiseWemResource o -> convertWwiseWemResource(o, game);
        };
    }

    private static Optional<Audio> convertWwiseWemLocalizedResource(WwiseWemLocalizedResource object, DS2Game game) {
        assert object.format().isStreaming();

        var dataSource = object.localizedDataSource(game.getSpokenLanguage());
        var data = game.readDataSource(dataSource.streamingDataSource());

        return convertWem(data);
    }

    private static Optional<Audio> convertWwiseWemResource(WwiseWemResource object, DS2Game game) {
        var data = object.format().isStreaming()
            ? game.readDataSource(object.data().streamingDataSource())
            : object.data().wemData();

        return convertWem(data);
    }

    private static Optional<Audio> convertWem(byte[] data) {
        RiffFile file;

        try {
            file = RIFF_PARSER.parse(BinaryReader.wrap(data));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var fmt = file.get(WwiseFmtChunk.ID).orElseThrow();
        var codec = new AudioCodec.Wwise();
        var format = new AudioFormat(fmt.sampleRate(), fmt.channelCount());

        return Optional.of(new Audio(codec, format, fmt.sampleCount(), data));
    }
}
