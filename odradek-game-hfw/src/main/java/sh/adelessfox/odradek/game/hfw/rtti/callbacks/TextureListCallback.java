package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.TextureList;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.TextureListEntryInfo;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class TextureListCallback implements ExtraBinaryDataCallback<TextureList> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, TextureList object) throws IOException {
        object.entries(reader.readObjects(reader.readInt(), r -> readEntry(r, factory)));
    }

    static TextureListEntryInfo readEntry(BinaryReader reader, TypeFactory factory) throws IOException {
        var entry = factory.newInstance(TextureListEntryInfo.class);
        entry.streamingOffset(reader.readInt());
        entry.streamingLength(reader.readInt());
        entry.texture(TextureCallback.read(reader, factory));
        return entry;
    }
}
