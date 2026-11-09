package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class ShaderResourceCallback implements ExtraBinaryDataCallback<HFW.ShaderResource> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.ShaderResource object) throws IOException {
        var size = source.readInt();
        object.hash(HFWTypeReader.readCompound(HFW.MurmurHashValue.class, source, factory));
        object.data(source.readBytes(size));
    }
}
