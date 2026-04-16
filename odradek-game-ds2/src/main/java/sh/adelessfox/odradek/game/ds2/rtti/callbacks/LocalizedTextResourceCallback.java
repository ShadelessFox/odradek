package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.extensions.ELanguageExtension;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;

public final class LocalizedTextResourceCallback implements ExtraBinaryDataCallback<DS2.LocalizedTextResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DS2.LocalizedTextResource object) throws IOException {
        var count = ELanguageExtension.writtenLanguages().size();
        var texts = new ArrayList<DS2.LocalizedTextResourceText>(count);
        for (int i = 0; i < count; i++) {
            var entry = factory.newInstance(DS2.LocalizedTextResourceText.class);
            entry.text(reader.readString(Short.toUnsignedInt(reader.readShort())));
            entry.altText(reader.readString(Short.toUnsignedInt(reader.readShort())));
            entry.mode(DS2.ESubtitleMode.valueOf(reader.readByte()));
            texts.add(entry);
        }
        object.texts(texts);
    }
}
