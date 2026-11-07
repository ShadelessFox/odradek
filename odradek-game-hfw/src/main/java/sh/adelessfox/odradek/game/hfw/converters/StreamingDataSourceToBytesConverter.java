package sh.adelessfox.odradek.game.hfw.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import wtf.reversed.toolbox.collect.Bytes;

import java.util.Optional;

public class StreamingDataSourceToBytesConverter implements Converter<HFW.StreamingDataSource, Bytes, HFWGame> {
    @Override
    public Optional<Bytes> convert(HFW.StreamingDataSource object, HFWGame game) {
        return Optional.of(game.readDataSource(object));
    }
}
