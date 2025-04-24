package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class DebugMouseCursorPS4Callback implements ExtraBinaryDataCallback<DebugMouseCursorPS4Callback.DebugMouseCursorData> {
    public interface DebugMouseCursorData {
        @Attr(name = "Stride", type = "uint32", position = 0, offset = 0)
        int stride();

        void stride(int value);

        @Attr(name = "Data", type = "Array<uint8>", position = 1, offset = 0)
        byte[] data();

        void data(byte[] value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DebugMouseCursorData object) throws IOException {
        object.stride(reader.readInt());
        object.data(reader.readBytes(reader.readInt()));
    }
}
