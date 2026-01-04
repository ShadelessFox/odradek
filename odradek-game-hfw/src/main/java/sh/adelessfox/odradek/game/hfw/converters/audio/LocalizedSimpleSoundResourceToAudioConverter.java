package sh.adelessfox.odradek.game.hfw.converters.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EWaveDataEncoding;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedSimpleSoundResource;

import java.util.Optional;

public class LocalizedSimpleSoundResourceToAudioConverter
    extends BaseAudioConverter<LocalizedSimpleSoundResource>
    implements Converter<LocalizedSimpleSoundResource, Audio, ForbiddenWestGame> {

    private static final Logger log = LoggerFactory.getLogger(LocalizedSimpleSoundResourceToAudioConverter.class);

    @Override
    public Optional<Audio> convert(LocalizedSimpleSoundResource object, ForbiddenWestGame game) {
        var properties = object.streaming().sharedWaveProperties();
        var localizedDataSource = object.streaming().localizedDataSources().getFirst();

        assert properties.soundSettings().isStreaming();

        var encoding = EWaveDataEncoding.valueOf(properties.waveFormat().encoding());
        var data = game.readDataSourceUnchecked(localizedDataSource.streamingDataSource());

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
}
