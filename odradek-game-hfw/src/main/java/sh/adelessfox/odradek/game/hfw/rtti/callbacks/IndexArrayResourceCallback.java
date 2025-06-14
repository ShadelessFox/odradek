package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.MurmurHashValue;
import sh.adelessfox.odradek.game.hfw.rtti.data.EIndexFormat;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class IndexArrayResourceCallback implements ExtraBinaryDataCallback<IndexArrayResourceCallback.IndexArrayData> {
    public interface IndexArrayData {
        @Attr(name = "Count", type = "uint32", position = 0)
        int count();

        void count(int value);

        @Attr(name = "Flags", type = "uint32", position = 1)
        int flags();

        void flags(int value);

        @Attr(name = "Format", type = "EIndexFormat", position = 2)
        EIndexFormat format();

        void format(EIndexFormat value);

        @Attr(name = "Checksum", type = "MurmurHashValue", position = 3)
        MurmurHashValue hash();

        void hash(MurmurHashValue value);

        @Attr(name = "IsStreaming", type = "bool", position = 4)
        boolean streaming();

        void streaming(boolean value);

        @Attr(name = "Data", type = "Array<uint8>", position = 5)
        byte[] data();

        void data(byte[] value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, IndexArrayData object) throws IOException {
        var count = reader.readInt();
        var flags = reader.readInt();
        var format = EIndexFormat.valueOf(reader.readInt());
        var streaming = reader.readIntBoolean();
        var hash = HFWTypeReader.readCompound(MurmurHashValue.class, reader, factory);
        var data = streaming ? null : reader.readBytes(count * format.stride());

        object.count(count);
        object.flags(flags);
        object.format(format);
        object.streaming(streaming);
        object.hash(hash);
        object.data(data);
    }
}
