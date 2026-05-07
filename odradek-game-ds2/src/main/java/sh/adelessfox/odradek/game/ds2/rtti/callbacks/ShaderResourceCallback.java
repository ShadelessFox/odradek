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
        object.unk04(DS2TypeReader.readCompound(DS2.GGUUID.class, reader, factory));
        object.unk14(DS2TypeReader.readCompound(DS2.GGUUID.class, reader, factory));
        object.unk24(DS2TypeReader.readCompound(DS2.StreamingDataSource.class, reader, factory));
    }
}
