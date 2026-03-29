package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2.LocalizedTextResource;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.LocalizedTextResourceText;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;

public final class LocalizedTextResourceCallback implements ExtraBinaryDataCallback<LocalizedTextResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, LocalizedTextResource object) throws IOException {
        var texts = new ArrayList<LocalizedTextResourceText>(27);
        for (int i = 0; i < 27; i++) {
            var text = factory.newInstance(LocalizedTextResourceText.class);
            text.text(reader.readString(Short.toUnsignedInt(reader.readShort())));
            text.notes(reader.readString(Short.toUnsignedInt(reader.readShort())));
            text.mode(reader.readByte());
            texts.add(text);
        }
        object.texts(texts);
    }
}
