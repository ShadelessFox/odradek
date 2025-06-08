package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class DataBufferResourceCallback implements ExtraBinaryDataCallback<DataBufferResourceCallback.DataBuffer> {
    public interface DataBuffer {
        @Attr(name = "Count", type = "uint32", position = 0)
        int count();

        void count(int value);

        @Attr(name = "Streaming", type = "bool", position = 1)
        boolean streaming();

        void streaming(boolean value);

        @Attr(name = "Flags", type = "uint32", position = 2)
        int flags();

        void flags(int value);

        @Attr(name = "Format", type = "uint32", position = 3)
        int format();

        void format(int value);

        @Attr(name = "Stride", type = "uint32", position = 4)
        int stride();

        void stride(int value);

        @Attr(name = "Data", type = "Array<uint8>", position = 5)
        byte[] data();

        void data(byte[] value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DataBuffer object) throws IOException {
        var count = reader.readInt();
        if (count == 0) {
            return;
        }

        var streaming = reader.readIntBoolean();
        var flags = reader.readInt();
        var format = reader.readInt();
        var stride = reader.readInt();
        var data = streaming ? null : reader.readBytes(stride * count);

        object.count(count);
        object.streaming(streaming);
        object.flags(flags);
        object.format(format);
        object.stride(stride);
        object.data(data);
    }
}
