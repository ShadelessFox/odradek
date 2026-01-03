package sh.adelessfox.odradek.game.hfw.converters.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.reader.AudioReader;
import sh.adelessfox.odradek.audio.reader.AudioReaderAtrac9;
import sh.adelessfox.odradek.audio.reader.AudioReaderWave;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

abstract class BaseAudioConverter<T> implements Converter<T, Audio, ForbiddenWestGame> {
    private static final Logger log = LoggerFactory.getLogger(BaseAudioConverter.class);

    protected static Optional<Audio> convert(HorizonForbiddenWest.EWaveDataEncoding encoding, byte[] data) {
        var reader = mapEncodingToReader(encoding).orElse(null);
        if (reader == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(reader.read(BinaryReader.wrap(data)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static Optional<AudioReader> mapEncodingToReader(HorizonForbiddenWest.EWaveDataEncoding encoding) {
        return Optional.ofNullable(switch (encoding) {
            case PCM -> new AudioReaderWave();
            case ATRAC9 -> new AudioReaderAtrac9();
            default -> {
                log.debug("Unsupported wave encoding: {}", encoding);
                yield null;
            }
        });
    }
}
