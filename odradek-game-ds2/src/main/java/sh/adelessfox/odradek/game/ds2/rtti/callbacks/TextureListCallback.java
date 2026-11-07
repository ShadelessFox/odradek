package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class TextureListCallback implements ExtraBinaryDataCallback<DS2.TextureList> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, DS2.TextureList object) throws IOException {
        object.entries(source.readObjects(source.readInt(), r -> readEntry(r, factory)));
    }

    static DS2.TextureListEntryInfo readEntry(BinarySource source, TypeFactory factory) throws IOException {
        var entry = factory.newInstance(DS2.TextureListEntryInfo.class);
        entry.streamingOffset(source.readInt());
        entry.streamingLength(source.readInt());
        entry.texture(TextureCallback.read(source, factory));
        return entry;
    }
}
