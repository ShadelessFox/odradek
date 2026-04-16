package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class ShaderResourceCallback implements ExtraBinaryDataCallback<HFW.ShaderResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, HFW.ShaderResource object) throws IOException {
        var size = reader.readInt();
        object.hash(HFWTypeReader.readCompound(HFW.MurmurHashValue.class, reader, factory));
        object.data(reader.readBytes(size));
    }
}
