package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class MorphemeAssetCallback implements ExtraBinaryDataCallback<MorphemeAssetCallback.MorphemeAssetData> {
    public interface MorphemeAssetData {
        @Attr(name = "Data", type = "Array<uint8>", position = 0)
        byte[] data();

        void data(byte[] value);

        @Attr(name = "Unk02", type = "uint32", position = 1)
        int unk02();

        void unk02(int value);

        @Attr(name = "Unk03", type = "uint32", position = 2)
        int unk03();

        void unk03(int value);

        @Attr(name = "Unk04", type = "uint64", position = 3)
        long unk04();

        void unk04(long value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, MorphemeAssetData object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
        object.unk02(reader.readInt());
        object.unk03(reader.readInt());
        object.unk04(reader.readLong());
    }
}
