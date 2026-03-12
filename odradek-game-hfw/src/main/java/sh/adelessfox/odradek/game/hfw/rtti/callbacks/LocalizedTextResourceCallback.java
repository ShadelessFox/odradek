package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedTextResource;
import sh.adelessfox.odradek.game.hfw.rtti.extensions.ELanguageExtension;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.io.StringFormat;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;

public class LocalizedTextResourceCallback implements ExtraBinaryDataCallback<LocalizedTextResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, LocalizedTextResource object) throws IOException {
        var count = ELanguageExtension.writtenLanguages().size();
        var texts = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) {
            texts.add(reader.readString(StringFormat.SHORT_LENGTH));
        }
        object.texts(texts);
    }

    @Override
    public void serialize(BinaryWriter writer, LocalizedTextResource object) throws IOException {
        for (String text : object.texts()) {
            writer.writeString(text, StringFormat.SHORT_LENGTH);
        }
    }
}
