package sh.adelessfox.odradek.game.kz4.rtti.callbacks;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class ShaderResourceCallback implements ExtraBinaryDataCallback<ShaderResourceCallback.ShaderData> {
    public interface ShaderData {
        @Attr(name = "Data", type = "Array<uint8>", position = 1, offset = 0)
        byte[] data();

        void data(byte[] value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, ShaderData object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
    }
}
