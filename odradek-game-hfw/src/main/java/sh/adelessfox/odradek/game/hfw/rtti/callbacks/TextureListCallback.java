package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.List;

public class TextureListCallback implements ExtraBinaryDataCallback<TextureListCallback.TextureListData> {
    public interface TextureListData {
        @Attr(name = "Entries", type = "Array<TextureEntry>", position = 0, offset = 0)
        List<TextureEntry> entries();

        void entries(List<TextureEntry> value);
    }

    public interface TextureEntry {
        @Attr(name = "StreamingOffset", type = "uint32", position = 0, offset = 0)
        int streamingOffset();

        void streamingOffset(int value);

        @Attr(name = "StreamingLength", type = "uint32", position = 1, offset = 0)
        int streamingLength();

        void streamingLength(int value);

        @Attr(name = "Data", type = "TextureData", position = 2, offset = 0)
        TextureCallback.TextureData data();

        void data(TextureCallback.TextureData value);

        static TextureEntry read(BinaryReader reader, TypeFactory factory) throws IOException {
            var entry = factory.newInstance(TextureEntry.class);
            entry.streamingOffset(reader.readInt());
            entry.streamingLength(reader.readInt());
            entry.data(TextureCallback.TextureData.read(reader, factory));
            return entry;
        }
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, TextureListData object) throws IOException {
        object.entries(reader.readObjects(reader.readInt(), r -> TextureEntry.read(r, factory)));
    }
}
