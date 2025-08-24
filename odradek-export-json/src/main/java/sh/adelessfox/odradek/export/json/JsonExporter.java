package sh.adelessfox.odradek.export.json;

import com.google.gson.FormattingStyle;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonWriter;
import sh.adelessfox.odradek.export.Exporter;
import sh.adelessfox.odradek.rtti.runtime.*;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JsonExporter implements Exporter<TypedObject> {
    @Override
    public void export(TypedObject object, WritableByteChannel channel) throws IOException {
        try (JsonWriter writer = new JsonWriter(Channels.newWriter(channel, StandardCharsets.UTF_8))) {
            writer.setStrictness(Strictness.STRICT);
            writer.setHtmlSafe(false);
            writer.setFormattingStyle(FormattingStyle.PRETTY);

            write(writer, object.getType(), object);
        }
    }

    @Override
    public String name() {
        return "JSON (JavaScript Object Notation)";
    }

    @Override
    public String extension() {
        return "json";
    }

    private static void write(JsonWriter writer, TypeInfo info, Object object) throws IOException {
        writer.beginObject();
        writer.name("$type").value(info.name().fullName());

        switch (info) {
            case AtomTypeInfo _ -> {
                writer.name("value");
                if (object instanceof Number number) {
                    writer.value(number);
                } else {
                    writer.value(String.valueOf(object));
                }
            }
            case ClassTypeInfo class_ -> {
                for (ClassAttrInfo attr : class_.displayableAttrs()) {
                    writer.name(attr.name());
                    write(writer, attr.type().get(), attr.get(object));
                }
            }
            case ContainerTypeInfo _ when object instanceof byte[] bytes -> {
                writer.name("data");
                writer.value(Base64.getEncoder().encodeToString(bytes));
            }
            case ContainerTypeInfo container -> {
                writer.name("items");
                writer.beginArray();
                for (int i = 0, length = container.length(object); i < length; i++) {
                    write(writer, container.itemType().get(), container.get(object, i));
                }
                writer.endArray();
            }
            case EnumTypeInfo _, PointerTypeInfo _ -> {
                writer.name("value").value(String.valueOf(object));
            }
        }

        writer.endObject();
    }
}
