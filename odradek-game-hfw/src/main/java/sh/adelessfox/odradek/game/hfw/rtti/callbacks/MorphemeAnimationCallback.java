package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.MurmurHashValue;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class MorphemeAnimationCallback implements ExtraBinaryDataCallback<MorphemeAnimationCallback.MorphemeAnimationData> {
    public interface MorphemeAnimationData {
        @Attr(name = "Data", type = "Array<uint8>", position = 0)
        byte[] data();

        void data(byte[] value);

        @Attr(name = "Hash", type = "MurmurHashValue", position = 1)
        MurmurHashValue hash();

        void hash(MurmurHashValue value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, MorphemeAnimationData object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
        object.hash(HFWTypeReader.readCompound(MurmurHashValue.class, reader, factory));
    }
}
