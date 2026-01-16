package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedTextResource;
import sh.adelessfox.odradek.game.hfw.rtti.extensions.ELanguageExtension;
import sh.adelessfox.odradek.io.BinaryReader;
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
            texts.add(reader.readString(Short.toUnsignedInt(reader.readShort())));
        }
        object.texts(texts);
    }
}
