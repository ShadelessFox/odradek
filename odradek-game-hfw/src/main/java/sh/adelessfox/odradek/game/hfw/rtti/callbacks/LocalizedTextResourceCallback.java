package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ELanguage;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedTextResource;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;

public class LocalizedTextResourceCallback implements ExtraBinaryDataCallback<LocalizedTextResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, LocalizedTextResource object) throws IOException {
        var count = ELanguage.values().length - 1; // Excluding "Unknown"
        var translations = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) {
            translations.add(reader.readString(Short.toUnsignedInt(reader.readShort())));
        }
        object.translations(translations);
    }
}
