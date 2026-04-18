package sh.adelessfox.odradek.game.ds2.converters.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;

import java.util.Optional;

public final class LocalizedSimpleSoundResourceToAudioConverter
    extends BaseAudioConverter<DS2.LocalizedSimpleSoundResource>
    implements Converter<DS2.LocalizedSimpleSoundResource, Audio, DS2Game> {

    @Override
    public Optional<Audio> convert(DS2.LocalizedSimpleSoundResource object, DS2Game game) {
        return convertLocalizedSimpleSoundResource(object, game);
    }

    private static Optional<Audio> convertLocalizedSimpleSoundResource(
        DS2.LocalizedSimpleSoundResource object,
        DS2Game game
    ) {
        var dataSource = object.localizedDataSource(game.getSpokenLanguage());
        var data = game.readDataSource(dataSource.streamingDataSource());

        return convertWem(data);
    }

}
