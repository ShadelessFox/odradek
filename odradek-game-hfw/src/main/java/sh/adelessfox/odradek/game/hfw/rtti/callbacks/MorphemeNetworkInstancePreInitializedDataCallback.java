package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class MorphemeNetworkInstancePreInitializedDataCallback implements ExtraBinaryDataCallback<MorphemeNetworkInstancePreInitializedDataCallback.MorphemeNetworkInstancePreInitializedData> {
    public interface MorphemeNetworkInstancePreInitializedData {
        @Attr(name = "Unk1", type = "Array<uint8>", position = 0)
        byte[] unk1();

        void unk1(byte[] value);

        @Attr(name = "Unk2", type = "Array<uint32>", position = 1)
        int[] unk2();

        void unk2(int[] value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, MorphemeNetworkInstancePreInitializedData object) throws IOException {
        var count = reader.readInt();
        var unk04 = reader.readInt();
        if (unk04 != 4) {
            throw new IOException("Value expected to be 4, was " + unk04);
        }
        if (count > 0) {
            object.unk1(reader.readBytes(count));
            object.unk2(reader.readInts(reader.readInt()));
        }
    }
}
