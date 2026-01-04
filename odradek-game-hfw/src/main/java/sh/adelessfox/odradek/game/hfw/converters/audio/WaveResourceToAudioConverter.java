package sh.adelessfox.odradek.game.hfw.converters.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.WaveResource;

import java.util.Optional;

public class WaveResourceToAudioConverter
    extends BaseAudioConverter<WaveResource>
    implements Converter<WaveResource, Audio, ForbiddenWestGame> {

    private static final Logger log = LoggerFactory.getLogger(WaveResourceToAudioConverter.class);

    @Override
    public Optional<Audio> convert(WaveResource object, ForbiddenWestGame game) {
        var encoding = object.format().encoding().unwrap();
        var data = object.format().isStreaming()
            ? game.readDataSourceUnchecked(object.data().streamingDataSource())
            : object.data().waveData();

        return switch (encoding) {
            case ATRAC9 -> convertAtrac9(data);
            case PCM -> convertPcm(
                Short.toUnsignedInt(object.format().bitsPerSample()),
                Short.toUnsignedInt(object.format().sampleRate()),
                Byte.toUnsignedInt(object.format().channelCount()),
                object.format().sampleCount(),
                data
            );
            default -> {
                log.debug("Unsupported wave encoding: {}", encoding);
                yield Optional.empty();
            }
        };
    }
}
