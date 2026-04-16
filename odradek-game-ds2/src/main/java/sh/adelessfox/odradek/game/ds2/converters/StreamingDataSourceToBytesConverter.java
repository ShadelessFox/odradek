package sh.adelessfox.odradek.game.ds2.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.StreamingDataSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

public final class StreamingDataSourceToBytesConverter implements Converter<StreamingDataSource, byte[], DS2Game> {
    @Override
    public Optional<byte[]> convert(StreamingDataSource object, DS2Game game) {
        try {
            return Optional.of(game.getDataSourceData(object));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
