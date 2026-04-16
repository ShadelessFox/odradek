package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;

public interface StreamingDataSourceExtension {
    default int fileId() {
        var dataSource = (DS2.StreamingDataSource) this;
        return (int) (dataSource.locator() & 0xffffff);
    }

    default int fileOffset() {
        var dataSource = (DS2.StreamingDataSource) this;
        return (int) (dataSource.locator() >>> 24);
    }

    default boolean isValid() {
        var dataSource = (DS2.StreamingDataSource) this;
        return dataSource.channel() != -1 /* EStreamingDataChannel.Invalid */ && dataSource.length() > 0;
    }

    default boolean isPresent() {
        var dataSource = (DS2.StreamingDataSource) this;
        return dataSource.locator() != 0 && dataSource.length() > 0;
    }
}
