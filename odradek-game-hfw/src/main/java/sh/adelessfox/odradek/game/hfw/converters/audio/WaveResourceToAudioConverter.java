package sh.adelessfox.odradek.game.hfw.converters.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.reader.AudioReaderAtrac9;
import sh.adelessfox.odradek.audio.reader.AudioReaderWave;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

public class WaveResourceToAudioConverter implements Converter<HorizonForbiddenWest.WaveResource, Audio, ForbiddenWestGame> {
    private static final Logger log = LoggerFactory.getLogger(WaveResourceToAudioConverter.class);

    @Override
    public Optional<Audio> convert(HorizonForbiddenWest.WaveResource object, ForbiddenWestGame game) {
        var reader = switch (object.format().encoding().unwrap()) {
            case PCM -> new AudioReaderWave();
            case ATRAC9 -> new AudioReaderAtrac9();
            default -> {
                log.debug("Unsupported wave encoding: {}", object.format().encoding());
                yield null;
            }
        };

        if (reader == null) {
            return Optional.empty();
        }

        var data = object.format().isStreaming()
            ? game.readDataSourceUnchecked(object.data().streamingDataSource())
            : object.data().waveData();

        try {
            return Optional.of(reader.read(BinaryReader.wrap(data)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
