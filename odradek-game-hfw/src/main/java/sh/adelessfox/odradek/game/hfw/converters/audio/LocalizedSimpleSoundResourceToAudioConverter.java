package sh.adelessfox.odradek.game.hfw.converters.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EWaveDataEncoding;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedSimpleSoundResource;

import java.util.Optional;

public class LocalizedSimpleSoundResourceToAudioConverter
    extends BaseAudioConverter<LocalizedSimpleSoundResource>
    implements Converter<LocalizedSimpleSoundResource, Audio, ForbiddenWestGame> {

    @Override
    public Optional<Audio> convert(LocalizedSimpleSoundResource object, ForbiddenWestGame game) {
        var properties = object.streaming().sharedWaveProperties();
        var localizedDataSources = object.streaming().localizedDataSources();

        assert properties.soundSettings().isStreaming();

        var encoding = EWaveDataEncoding.valueOf(properties.waveFormat().encoding());
        var data = game.readDataSourceUnchecked(localizedDataSources.getFirst().streamingDataSource());

        return convert(encoding, data);
    }
}
