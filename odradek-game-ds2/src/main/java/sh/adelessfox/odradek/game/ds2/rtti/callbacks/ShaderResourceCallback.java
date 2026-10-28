package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public final class ShaderResourceCallback implements ExtraBinaryDataCallback<DS2.ShaderResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DS2.ShaderResource object) throws IOException {
        object.size(reader.readInt());
        object.shaderCacheKey(reader.readInts(4));
        object.shaderIdentifier(reader.readInts(4));

        var dataSource = DS2TypeReader.readCompound(DS2.StreamingDataSource.class, reader, factory);
        object.streamingDataSource().channel(dataSource.channel());
        object.streamingDataSource().offset(dataSource.offset());
        object.streamingDataSource().length(dataSource.length());
    }
}
