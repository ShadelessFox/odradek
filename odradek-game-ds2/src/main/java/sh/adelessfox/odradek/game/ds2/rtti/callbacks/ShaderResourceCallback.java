package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public final class ShaderResourceCallback implements ExtraBinaryDataCallback<DS2.ShaderResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DS2.ShaderResource object) throws IOException {
        var size = reader.readInt();
        var unk00 = reader.readBytes(32);
        var unk20 = reader.readByte();
        var unk21 = reader.readInt();
        var unk25 = reader.readInt();
    }
}
