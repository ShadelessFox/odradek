package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.MurmurHashValue;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class ShaderResourceCallback implements ExtraBinaryDataCallback<ShaderResourceCallback.ShaderData> {
    public interface ShaderData {
        @Attr(name = "Hash", type = "MurmurHashValue", position = 0)
        MurmurHashValue hash();

        void hash(MurmurHashValue hash);

        @Attr(name = "Data", type = "Array<uint8>", position = 1)
        byte[] data();

        void data(byte[] data);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, ShaderData object) throws IOException {
        var size = reader.readInt();
        object.hash(HFWTypeReader.readCompound(MurmurHashValue.class, reader, factory));
        object.data(reader.readBytes(size));
    }
}
