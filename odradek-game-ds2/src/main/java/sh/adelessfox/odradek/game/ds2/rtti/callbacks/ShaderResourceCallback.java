package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public final class ShaderResourceCallback implements ExtraBinaryDataCallback<DS2.ShaderResource> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, DS2.ShaderResource object) throws IOException {
        object.size(source.readInt());
        object.shaderCacheKey(source.readInts(4));
        object.shaderIdentifier(source.readInts(4));

        var dataSource = DS2TypeReader.readCompound(DS2.StreamingDataSource.class, source, factory);
        object.streamingDataSource().channel(dataSource.channel());
        object.streamingDataSource().offset(dataSource.offset());
        object.streamingDataSource().length(dataSource.length());
    }
}
