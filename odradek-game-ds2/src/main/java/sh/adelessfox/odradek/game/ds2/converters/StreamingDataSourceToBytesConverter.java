package sh.adelessfox.odradek.game.ds2.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import wtf.reversed.toolbox.collect.Bytes;

import java.util.Optional;

public final class StreamingDataSourceToBytesConverter implements Converter<DS2.StreamingDataSource, Bytes, DS2Game> {
    @Override
    public Optional<Bytes> convert(DS2.StreamingDataSource object, DS2Game game) {
        return Optional.of(game.readDataSource(object));
    }
}
