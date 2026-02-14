package sh.adelessfox.odradek.game.untildawn.rtti.callbacks;

import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.ShaderResource;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class ShaderResourceCallback implements ExtraBinaryDataCallback<ShaderResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, ShaderResource object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
    }
}
