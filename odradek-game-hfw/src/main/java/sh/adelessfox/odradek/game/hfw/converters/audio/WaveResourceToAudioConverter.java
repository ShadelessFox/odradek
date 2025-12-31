package sh.adelessfox.odradek.game.hfw.converters.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioEncoding;
import sh.adelessfox.odradek.audio.AudioFormat;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;

import java.util.Optional;

public class WaveResourceToAudioConverter implements Converter<HorizonForbiddenWest.WaveResource, Audio, ForbiddenWestGame> {
    private static final Logger log = LoggerFactory.getLogger(WaveResourceToAudioConverter.class);

    @Override
    public Optional<Audio> convert(HorizonForbiddenWest.WaveResource object, ForbiddenWestGame game) {
        var encoding = mapEncoding(object.format().encoding().unwrap()).orElse(null);
        if (encoding == null) {
            return Optional.empty();
        }

        var format = new AudioFormat(
            encoding,
            Short.toUnsignedInt(object.format().sampleRate()),
            Short.toUnsignedInt(object.format().bitsPerSample()),
            Byte.toUnsignedInt(object.format().channelCount())
        );

        var data = object.format().isStreaming()
            ? game.readDataSourceUnchecked(object.data().streamingDataSource())
            : object.data().waveData();

        return Optional.of(new Audio(format, data));
    }

    private static Optional<AudioEncoding> mapEncoding(HorizonForbiddenWest.EWaveDataEncoding encoding) {
        return Optional.ofNullable(switch (encoding) {
            case PCM -> AudioEncoding.PCM;
            case ATRAC9 -> AudioEncoding.ATRAC9;
            default -> {
                log.debug("Unsupported wave encoding: {}", encoding);
                yield null;
            }
        });
    }
}
