package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.extensions.ELanguageExtension;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalizedTextResourceCallback implements ExtraBinaryDataCallback<HFW.LocalizedTextResource> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.LocalizedTextResource object) throws IOException {
        var count = ELanguageExtension.writtenLanguages().size();
        var texts = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) {
            texts.add(source.readString(source.readUnsignedShort()));
        }
        object.texts(List.copyOf(texts));
    }
}
