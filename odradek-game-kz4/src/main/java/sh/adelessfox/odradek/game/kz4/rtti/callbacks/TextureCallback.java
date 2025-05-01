package sh.adelessfox.odradek.game.kz4.rtti.callbacks;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class TextureCallback implements ExtraBinaryDataCallback<TextureCallback.TextureInfo> {
    public interface TextureInfo {
        @Attr(name = "Header", type = "Array<uint8>", position = 0, offset = 0)
        byte[] header();

        void header(byte[] value);

        @Attr(name = "Data", type = "Array<uint8>", position = 1, offset = 0)
        byte[] data();

        void data(byte[] value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, TextureInfo object) throws IOException {
        object.header(reader.readBytes(40));
        object.data(reader.readBytes(reader.readInt()));
    }
}
