package sh.adelessfox.odradek.export.json;

import com.google.gson.FormattingStyle;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonWriter;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.util.SimpleTypeVisitor;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class JsonExporter implements Exporter.OfSingleOutput<TypedObject> {
    @Override
    public void export(TypedObject object, WritableByteChannel channel) throws IOException {
        try (JsonWriter writer = new JsonWriter(Channels.newWriter(channel, StandardCharsets.UTF_8))) {
            writer.setStrictness(Strictness.STRICT);
            writer.setHtmlSafe(false);
            writer.setFormattingStyle(FormattingStyle.PRETTY);

            new JsonVisitor().visit(object, writer);
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

    private static final class JsonVisitor extends SimpleTypeVisitor<JsonWriter, IOException> {
        @Override
        public void visitAtom(AtomTypeInfo info, Object object, JsonWriter writer) throws IOException {
            if (object instanceof Number number) {
                writer.value(number);
            } else {
                writer.value(String.valueOf(object));
            }
        }

        @Override
        public void visitClass(ClassTypeInfo info, Object object, JsonWriter writer) throws IOException {
            writer.beginObject();
            super.visitClass(info, object, writer);
            writer.endObject();
        }

        @Override
        public void visitClassAttr(
            ClassTypeInfo info,
            ClassAttrInfo attr,
            Object object,
            JsonWriter writer
        ) throws IOException {
            writer.name(attr.name());
            super.visitClassAttr(info, attr, object, writer);
        }

        @Override
        public void visitContainer(ContainerTypeInfo info, Object object, JsonWriter writer) throws IOException {
            if (object instanceof byte[] bytes) {
                writer.value(Base64.getEncoder().encodeToString(bytes));
            } else {
                writer.beginArray();
                super.visitContainer(info, object, writer);
                writer.endArray();
            }
        }

        @Override
        public void visitContainerItem(
            ContainerTypeInfo info,
            Object object,
            int index,
            JsonWriter writer
        ) throws IOException {
            super.visitContainerItem(info, object, index, writer);
        }

        @Override
        public void visitEnum(EnumTypeInfo info, Value<?> object, JsonWriter writer) throws IOException {
            writer.value(String.valueOf(object));
        }

        @Override
        public void visitPointer(PointerTypeInfo info, Object object, JsonWriter writer) throws IOException {
            writer.value(String.valueOf(object));
        }
    }
}
