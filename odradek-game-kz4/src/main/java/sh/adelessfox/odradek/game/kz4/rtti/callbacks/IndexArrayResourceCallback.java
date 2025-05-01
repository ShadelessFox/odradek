package sh.adelessfox.odradek.game.kz4.rtti.callbacks;

import sh.adelessfox.odradek.game.kz4.rtti.Killzone4.MurmurHashValue;
import sh.adelessfox.odradek.game.kz4.rtti.Killzone4TypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class IndexArrayResourceCallback implements ExtraBinaryDataCallback<IndexArrayResourceCallback.Indices> {
    public interface Indices {
        @Attr(name = "Count", type = "uint32", position = 0, offset = 0)
        int count();

        void count(int value);

        @Attr(name = "Flags", type = "uint32", position = 1, offset = 0)
        int flags();

        void flags(int value);

        @Attr(name = "Format", type = "uint32", position = 2, offset = 0)
        int format();

        void format(int value);

        @Attr(name = "Hash", type = "MurmurHashValue", position = 3, offset = 0)
        MurmurHashValue hash();

        void hash(MurmurHashValue hash);

        @Attr(name = "IsStreaming", type = "bool", position = 4, offset = 0)
        boolean streaming();

        void streaming(boolean value);

        @Attr(name = "Data", type = "Array<uint8>", position = 5, offset = 0)
        byte[] data();

        void data(byte[] value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, Indices object) throws IOException {
        object.count(reader.readInt());
        if (object.count() > 0) {
            object.flags(reader.readInt());
            object.format(reader.readInt());
            object.hash(Killzone4TypeReader.readCompound(MurmurHashValue.class, reader, factory));
            object.streaming(reader.readIntBoolean());
            object.data(object.streaming() ? null : reader.readBytes(switch (object.format()) {
                case 0 -> object.count() * Short.BYTES;
                case 1 -> object.count() * Integer.BYTES;
                default -> throw new IllegalArgumentException("Unexpected index format: " + object.format());
            }));
        }
    }
}
