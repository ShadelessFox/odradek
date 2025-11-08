package sh.adelessfox.odradek.export.json;

import com.google.gson.FormattingStyle;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonWriter;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class JsonExporter implements Exporter<TypedObject> {
    @Override
    public void export(TypedObject object, WritableByteChannel channel) throws IOException {
        try (JsonWriter writer = new JsonWriter(Channels.newWriter(channel, StandardCharsets.UTF_8))) {
            writer.setStrictness(Strictness.STRICT);
            writer.setHtmlSafe(false);
            writer.setFormattingStyle(FormattingStyle.PRETTY);

            write(writer, object.getType(), object, true);
        }
    }

    @Override
    public String id() {
        return "json";
    }

    @Override
    public String name() {
        return "JSON (JavaScript Object Notation)";
    }

    @Override
    public String extension() {
        return "json";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:json");
    }

    private static void write(JsonWriter writer, TypeInfo info, Object object, boolean root) throws IOException {
        switch (info) {
            case AtomTypeInfo _ -> {
                if (object instanceof Number number) {
                    writer.value(number);
                } else {
                    writer.value(String.valueOf(object));
                }
            }
            case ClassTypeInfo clazz -> {
                writer.beginObject();
                if (root) {
                    writer.name("$type").value(info.name());
                }
                for (ClassAttrInfo attr : clazz.serializedAttrs()) {
                    writer.name(attr.name());
                    write(writer, attr.type(), clazz.get(attr, object), false);
                }
                writer.endObject();
            }
            case ContainerTypeInfo _ when object instanceof byte[] bytes -> {
                writer.value(Base64.getEncoder().encodeToString(bytes));
            }
            case ContainerTypeInfo container -> {
                writer.beginArray();
                for (int i = 0, length = container.length(object); i < length; i++) {
                    write(writer, container.itemType(), container.get(object, i), false);
                }
                writer.endArray();
            }
            case EnumTypeInfo _, PointerTypeInfo _ -> {
                writer.value(String.valueOf(object));
            }
        }
    }
}
