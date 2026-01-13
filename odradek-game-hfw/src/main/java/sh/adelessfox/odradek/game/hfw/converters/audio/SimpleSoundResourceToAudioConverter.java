package sh.adelessfox.odradek.game.hfw.converters.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ELanguage;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedSimpleSoundResource;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.RandomSimpleSoundResource;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.SimpleSoundResource;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class SimpleSoundResourceToAudioConverter
    extends BaseAudioConverter<SimpleSoundResource>
    implements Converter<SimpleSoundResource, Audio, ForbiddenWestGame> {

    private static final Logger log = LoggerFactory.getLogger(SimpleSoundResourceToAudioConverter.class);

    @Override
    public Optional<Audio> convert(SimpleSoundResource object, ForbiddenWestGame game) {
        return switch (object) {
            case LocalizedSimpleSoundResource o -> convertLocalizedSimpleSoundResource(o, game);
            case RandomSimpleSoundResource o -> convertRandomSimpleSoundResource(o, game);
            default -> convertSimpleSoundResource(object, game);
        };
    }

    private static Optional<Audio> convertSimpleSoundResource(SimpleSoundResource object, ForbiddenWestGame game) {
        var resource = object.sound().wave();
        if (resource != null) {
            return Converter.convert(resource.get(), Audio.class, game);
        }
        return Optional.empty();
    }

    private static Optional<Audio> convertLocalizedSimpleSoundResource(LocalizedSimpleSoundResource object, ForbiddenWestGame game) {
        var properties = object.streaming().sharedWaveProperties();
        var localizedDataSource = localizedDataSourceForLanguage(object, game.getSpokenLanguage());

        assert properties.soundSettings().isStreaming();

        var encoding = HorizonForbiddenWest.EWaveDataEncoding.valueOf(properties.waveFormat().encoding());
        var data = game.readDataSource(localizedDataSource.streamingDataSource());

        return switch (encoding) {
            case ATRAC9 -> convertAtrac9(data);
            case PCM -> convertPcm(
                Short.toUnsignedInt(properties.waveFormat().bitsPerSample()),
                Short.toUnsignedInt(properties.waveFormat().sampleRate()),
                Byte.toUnsignedInt(properties.waveFormat().channelCount()),
                localizedDataSource.sampleCount(),
                data
            );
            default -> {
                log.debug("Unsupported wave encoding: {}", encoding);
                yield Optional.empty();
            }
        };
    }

    private static Optional<Audio> convertRandomSimpleSoundResource(RandomSimpleSoundResource object, ForbiddenWestGame game) {
        // TODO: Return all variants once Audio can contain multiple tracks
        return Optional.empty();
    }

    private static HorizonForbiddenWest.LocalizedDataSource localizedDataSourceForLanguage(LocalizedSimpleSoundResource resource, ELanguage language) {
        var supportedLanguages = Stream.of(ELanguage.values())
            .sorted(Comparator.comparingInt(ELanguage::value))
            .filter(x -> (languageFlags(x) & 2) != 0)
            .toList();

        int index = supportedLanguages.indexOf(language);
        var dataSources = resource.streaming().localizedDataSources();
        return index < 0 ? dataSources.getFirst() : dataSources.get(index);
    }

    private static int languageFlags(ELanguage language) {
        // Same code as in HZD. Still no idea what these flags actually mean
        // FF C9 83 F9 14 77 24 48 63 C1 48 8D 15 ? ? ? ? 0F B6 84 02 ? ? ? ? 8B 8C 82 ? ? ? ? 48 03 CA FF E1
        return switch (language.value()) {
            case 1 -> 7;
            case 2, 3, 4, 5, 7, 10, 11, 16, 17, 18, 20 -> 3;
            default -> 1;
        };
    }
}
