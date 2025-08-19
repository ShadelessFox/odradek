package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingDataSource;
import sh.adelessfox.odradek.rtti.Attr;

public interface StreamingDataSourceExtension {
    @Attr(name = "Locator", type = "uint64", position = 0, offset = 0)
    long locator();

    void locator(long locator);

    default int fileId() {
        return (int) (locator() & 0xffffff);
    }

    default int fileOffset() {
        return (int) (locator() >>> 24);
    }

    default boolean isValid() {
        var dataSource = (StreamingDataSource) this;
        return dataSource.channel() != -1 /* EStreamingDataChannel.Invalid */ && dataSource.length() > 0;
    }
}
