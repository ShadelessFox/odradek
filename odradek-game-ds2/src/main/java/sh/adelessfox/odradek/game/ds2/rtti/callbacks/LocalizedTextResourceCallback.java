package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.extensions.ELanguageExtension;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.io.StringFormat;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.util.Check;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class LocalizedTextResourceCallback implements ExtraBinaryDataCallback<DS2.LocalizedTextResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DS2.LocalizedTextResource object) throws IOException {
        var count = ELanguageExtension.writtenLanguages().size();
        var texts = new ArrayList<DS2.LocalizedTextResourceText>(count);
        for (int i = 0; i < count; i++) {
            var entry = factory.newInstance(DS2.LocalizedTextResourceText.class);
            entry.text(reader.readString(StringFormat.SHORT_LENGTH));
            entry.altText(reader.readString(StringFormat.SHORT_LENGTH));
            entry.mode(DS2.ESubtitleMode.valueOf(reader.readByte()));
            texts.add(entry);
        }
        object.texts(List.copyOf(texts));
    }

    @Override
    public void serialize(BinaryWriter writer, DS2.LocalizedTextResource object) throws IOException {
        Check.state(object.texts().size() == ELanguageExtension.writtenLanguages().size(), "texts count mismatch");
        for (DS2.LocalizedTextResourceText text : object.texts()) {
            writer.writeString(text.text(), StringFormat.SHORT_LENGTH);
            writer.writeString(text.altText(), StringFormat.SHORT_LENGTH);
            writer.writeByte((byte) text.mode().value());
        }
    }
}
