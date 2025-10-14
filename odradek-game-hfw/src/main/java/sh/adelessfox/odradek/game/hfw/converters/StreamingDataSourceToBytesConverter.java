package sh.adelessfox.odradek.game.hfw.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingDataSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.Set;

public class StreamingDataSourceToBytesConverter implements Converter<ForbiddenWestGame, byte[]> {
    @Override
    public Optional<byte[]> convert(Object object, ForbiddenWestGame game) {
        try {
            var dataSource = (StreamingDataSource) object;
            var data = game.getStreamingSystem().getDataSourceData(dataSource);
            return Optional.of(data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Set<Class<?>> supportedTypes() {
        return Set.of(StreamingDataSource.class);
    }
}
