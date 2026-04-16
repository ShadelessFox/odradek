package sh.adelessfox.odradek.game.hfw.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

public class StreamingDataSourceToBytesConverter implements Converter<HFW.StreamingDataSource, byte[], HFWGame> {
    @Override
    public Optional<byte[]> convert(HFW.StreamingDataSource object, HFWGame game) {
        try {
            return Optional.of(game.getDataSourceData(object));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
