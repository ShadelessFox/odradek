package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class TextureListCallback implements ExtraBinaryDataCallback<HFW.TextureList> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.TextureList object) throws IOException {
        object.entries(source.readObjects(source.readInt(), r -> readEntry(r, factory)));
    }

    static HFW.TextureListEntryInfo readEntry(BinarySource source, TypeFactory factory) throws IOException {
        var entry = factory.newInstance(HFW.TextureListEntryInfo.class);
        entry.streamingOffset(source.readInt());
        entry.streamingLength(source.readInt());
        entry.texture(TextureCallback.read(source, factory));
        return entry;
    }
}
