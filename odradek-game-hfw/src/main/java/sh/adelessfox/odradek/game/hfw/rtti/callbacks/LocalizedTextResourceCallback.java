package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ELanguage;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalizedTextResourceCallback implements ExtraBinaryDataCallback<LocalizedTextResourceCallback.TranslationData> {
    public interface TranslationData {
        @Attr(name = "Translations", type = "Array<String>", position = 0, offset = 0)
        List<String> translations();

        void translations(List<String> translations);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, TranslationData object) throws IOException {
        var count = ELanguage.values().length - 1; // Excluding "Unknown"
        var translations = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) {
            translations.add(reader.readString(Short.toUnsignedInt(reader.readShort())));
        }
        object.translations(translations);
    }
}
