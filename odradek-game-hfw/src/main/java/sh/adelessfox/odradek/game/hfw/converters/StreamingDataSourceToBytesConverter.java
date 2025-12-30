package sh.adelessfox.odradek.game.hfw.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingDataSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

public class StreamingDataSourceToBytesConverter implements Converter<StreamingDataSource, byte[], ForbiddenWestGame> {
    @Override
    public Optional<byte[]> convert(StreamingDataSource object, ForbiddenWestGame game) {
        try {
            return Optional.of(game.getStreamingSystem().getDataSourceData(object));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
