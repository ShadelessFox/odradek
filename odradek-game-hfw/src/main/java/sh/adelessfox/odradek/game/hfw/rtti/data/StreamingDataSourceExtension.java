package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingDataSource;

public interface StreamingDataSourceExtension {
    default int fileId() {
        var dataSource = (StreamingDataSource) this;
        return (int) (dataSource.locator() & 0xffffff);
    }

    default int fileOffset() {
        var dataSource = (StreamingDataSource) this;
        return (int) (dataSource.locator() >>> 24);
    }

    default boolean isValid() {
        var dataSource = (StreamingDataSource) this;
        return dataSource.channel() != -1 /* EStreamingDataChannel.Invalid */ && dataSource.length() > 0;
    }
}
