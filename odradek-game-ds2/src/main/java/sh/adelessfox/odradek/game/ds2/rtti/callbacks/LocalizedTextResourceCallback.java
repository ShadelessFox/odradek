package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2.ESubtitleMode;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.LocalizedTextResource;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.LocalizedTextResourceText;
import sh.adelessfox.odradek.game.ds2.rtti.extensions.ELanguageExtension;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;

public final class LocalizedTextResourceCallback implements ExtraBinaryDataCallback<LocalizedTextResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, LocalizedTextResource object) throws IOException {
        var count = ELanguageExtension.writtenLanguages().size();
        var texts = new ArrayList<LocalizedTextResourceText>(count);
        for (int i = 0; i < count; i++) {
            var entry = factory.newInstance(LocalizedTextResourceText.class);
            entry.text(reader.readString(Short.toUnsignedInt(reader.readShort())));
            entry.altText(reader.readString(Short.toUnsignedInt(reader.readShort())));
            entry.mode(ESubtitleMode.valueOf(reader.readByte()));
            texts.add(entry);
        }
        object.texts(texts);
    }
}
