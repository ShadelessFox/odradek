package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class TextureListCallback implements ExtraBinaryDataCallback<DS2.TextureList> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DS2.TextureList object) throws IOException {
        object.entries(reader.readObjects(reader.readInt(), r -> readEntry(r, factory)));
    }

    static DS2.TextureListEntryInfo readEntry(BinaryReader reader, TypeFactory factory) throws IOException {
        var entry = factory.newInstance(DS2.TextureListEntryInfo.class);
        entry.streamingOffset(reader.readInt());
        entry.streamingLength(reader.readInt());
        entry.texture(TextureCallback.read(reader, factory));
        return entry;
    }
}
