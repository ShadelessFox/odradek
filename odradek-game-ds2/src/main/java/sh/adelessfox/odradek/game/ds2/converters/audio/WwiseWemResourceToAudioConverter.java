package sh.adelessfox.odradek.game.ds2.converters.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;

import java.util.Optional;

public final class WwiseWemResourceToAudioConverter
    extends BaseAudioConverter<DS2.WwiseWemResource>
    implements Converter<DS2.WwiseWemResource, Audio, DS2Game> {

    @Override
    public Optional<Audio> convert(DS2.WwiseWemResource object, DS2Game game) {
        return switch (object) {
            case DS2.WwiseWemLocalizedResource o -> convertWwiseWemLocalizedResource(o, game);
            case DS2.WwiseWemResource o -> convertWwiseWemResource(o, game);
        };
    }

    private static Optional<Audio> convertWwiseWemLocalizedResource(
        DS2.WwiseWemLocalizedResource object,
        DS2Game game
    ) {
        assert object.format().isStreaming();

        var dataSource = object.localizedDataSource(game.getSpokenLanguage());
        var data = game.readDataSource(dataSource.streamingDataSource());

        return convertWem(data);
    }

    private static Optional<Audio> convertWwiseWemResource(DS2.WwiseWemResource object, DS2Game game) {
        var data = object.format().isStreaming()
            ? game.readDataSource(object.data().streamingDataSource())
            : object.data().wemData();

        return convertWem(data);
    }
}
