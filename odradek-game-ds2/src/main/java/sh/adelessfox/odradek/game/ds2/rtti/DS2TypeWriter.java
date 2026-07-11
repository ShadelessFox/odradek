package sh.adelessfox.odradek.game.ds2.rtti;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.decima.DecimaHash;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.io.AbstractTypeWriter;
import wtf.reversed.toolbox.collect.Bytes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DS2TypeWriter extends AbstractTypeWriter {
    @Override
    protected void writeEnum(Value<?> object, EnumTypeInfo info, BinaryWriter writer) throws IOException {
        int value = object.value();
        switch (info.size()) {
            case Byte.BYTES -> writer.writeByte((byte) value);
            case Short.BYTES -> writer.writeShort((short) value);
            case Integer.BYTES -> writer.writeInt(value);
            default -> throw new IllegalArgumentException("Unexpected enum size: " + info.size());
        }
    }

    @Override
    protected void writeContainer(Object object, ContainerTypeInfo info, BinaryWriter writer) throws IOException {
        switch (info.containerType()) {
            case "HashMap", "HashSet" -> writeHashContainer(object, info, writer);
            default -> writeSimpleContainer(object, info, writer);
        };
    }

    @Override
    protected void writePointer(Object object, PointerTypeInfo info, BinaryWriter writer) throws IOException {
        throw new IOException("Unexpected pointer");
    }

    @Override
    protected AtomWriter writerForAtom(AtomTypeInfo info) {
        var name = info.base().name();
        return switch (name) {
            case "bool" -> AtomWriter.BOOL_8;
            case "uint8", "int8" -> AtomWriter.INT_8;
            case "uint16", "int16" -> AtomWriter.INT_16;
            case "uint", "int", "uint32", "int32", "ucs4" -> AtomWriter.INT_32;
            case "uint64", "int64", "uintptr" -> AtomWriter.INT_64;
            case "HalfFloat" -> AtomWriter.FLOAT_16;
            case "float" -> AtomWriter.FLOAT_32;
            case "double" -> AtomWriter.FLOAT_64;
            case "String" -> StringWriter.INSTANCE;
            case "WString" -> WStringWriter.INSTANCE;
            default -> throw new IllegalArgumentException("Unknown atom type: " + info.name() + " (" + name + ")");
        };
    }

    private void writeSimpleContainer(
        Object object,
        ContainerTypeInfo info,
        BinaryWriter writer
    ) throws IOException {
        var length = info.length(object);
        writer.writeInt(length);

        var itemInfo = info.itemType();
        if (itemInfo instanceof AtomTypeInfo atom) {
            writerForAtom(atom).write(writer, object, info);
        } else {
            for (int i = 0; i < length; i++) {
                write(info.get(object, i), itemInfo, writer);
            }
        }
    }

    private void writeHashContainer(
        Object object,
        ContainerTypeInfo info,
        BinaryWriter writer
    ) throws IOException {
        var length = info.length(object);
        writer.writeInt(length);

        var itemInfo = info.itemType();
        for (int i = 0; i < length; i++) {
            var item = info.get(object, i);
            var hash = computeHash(item, itemInfo);
            writer.writeInt(hash);
            write(item, itemInfo, writer);
        }
    }

    private int computeHash(Object value, TypeInfo info) {
        throw new NotImplementedException(); // TODO
    }

    private static final class StringWriter implements AtomWriter {
        static final StringWriter INSTANCE = new StringWriter();

        @Override
        public void write(BinaryWriter writer, Object value) throws IOException {
            var data = ((String) value).getBytes(StandardCharsets.UTF_8);
            var length = data.length;

            writer.writeInt(length);

            if (length > 0) {
                int hash = DecimaHash.crc32().hash(Bytes.wrap(data)).asInt() & 0x7fffffff;
                writer.writeInt(hash);
                writer.writeBytes(data);
            }
        }
    }

    private static final class WStringWriter implements AtomWriter {
        static final WStringWriter INSTANCE = new WStringWriter();

        @Override
        public void write(BinaryWriter writer, Object value) throws IOException {
            var data = ((String) value).getBytes(StandardCharsets.UTF_16LE);
            var length = data.length;

            writer.writeInt(length / 2);
            writer.writeBytes(data);
        }
    }
}
