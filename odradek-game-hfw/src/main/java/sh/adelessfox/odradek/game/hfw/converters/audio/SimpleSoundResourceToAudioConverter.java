package sh.adelessfox.odradek.game.hfw.converters.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.LocalizedSimpleSoundResource;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.RandomSimpleSoundResource;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.SimpleSoundResource;

import java.util.Optional;

public class SimpleSoundResourceToAudioConverter
    extends BaseAudioConverter<SimpleSoundResource>
    implements Converter<SimpleSoundResource, Audio, HFWGame> {

    private static final Logger log = LoggerFactory.getLogger(SimpleSoundResourceToAudioConverter.class);

    @Override
    public Optional<Audio> convert(SimpleSoundResource object, HFWGame game) {
        return switch (object) {
            case LocalizedSimpleSoundResource o -> convertLocalizedSimpleSoundResource(o, game);
            case RandomSimpleSoundResource o -> convertRandomSimpleSoundResource(o, game);
            default -> convertSimpleSoundResource(object, game);
        };
    }

    private static Optional<Audio> convertSimpleSoundResource(SimpleSoundResource object, HFWGame game) {
        var resource = object.sound().wave();
        if (resource != null) {
            return Converter.convert(resource.get(), Audio.class, game);
        }
        return Optional.empty();
    }

    private static Optional<Audio> convertLocalizedSimpleSoundResource(LocalizedSimpleSoundResource object, HFWGame game) {
        var properties = object.streaming().sharedWaveProperties();
        var localizedDataSource = object.localizedDataSource(game.getSpokenLanguage());

        assert properties.soundSettings().isStreaming();

        var encoding = HFW.EWaveDataEncoding.valueOf(properties.waveFormat().encoding());
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

    private static Optional<Audio> convertRandomSimpleSoundResource(RandomSimpleSoundResource object, HFWGame game) {
        // TODO: Return all variants once Audio can contain multiple tracks
        return Optional.empty();
    }
}
