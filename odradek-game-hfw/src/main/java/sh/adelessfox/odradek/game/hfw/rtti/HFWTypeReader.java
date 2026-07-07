package sh.adelessfox.odradek.game.hfw.rtti;

import sh.adelessfox.odradek.game.decima.DecimaHash;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.rtti.io.AbstractTypeReader;
import wtf.reversed.toolbox.collect.Bytes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HFWTypeReader extends AbstractTypeReader {
    public static <T> T readCompound(Class<T> cls, BinaryReader reader, TypeFactory factory) throws IOException {
        var type = factory.get(cls.getSimpleName()).asClass();
        return cls.cast(new HFWTypeReader().readCompound(type, reader, factory));
    }

    public TypedObject readObject(BinaryReader reader, TypeFactory factory) throws IOException {
        var hash = reader.readLong();
        var size = reader.readInt();
        var type = factory.get(HFWTypeId.of(hash)).asClass();

        var start = reader.position();
        var object = readCompound(type, reader, factory);
        var end = reader.position();

        if (end - start != size) {
            throw new IllegalStateException(
                "Size mismatch for %s: %d (actual) != %d (expected)".formatted(type.name(), end - start, size));
        }

        int numLinks = reader.readInt();
        if (numLinks != 0) {
            throw new IllegalStateException("Expected 0 links, got " + numLinks);
        }

        return object;
    }

    @Override
    protected Value<?> readEnum(EnumTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        int value = switch (info.size()) {
            case Byte.BYTES -> reader.readByte();
            case Short.BYTES -> reader.readShort();
            case Integer.BYTES -> reader.readInt();
            default -> throw new IllegalArgumentException("Unexpected enum size: " + info.size());
        };
        if (info instanceof EnumSetTypeInfo) {
            return info.setOf(value);
        } else {
            return info.valueOf(value);
        }
    }

    @Override
    protected Object readContainer(
        ContainerTypeInfo info,
        BinaryReader reader,
        TypeFactory factory
    ) throws IOException {
        return switch (info.containerType()) {
            case "HashMap", "HashSet" -> readHashContainer(info, reader, factory);
            default -> readSimpleContainer(info, reader, factory);
        };
    }

    @Override
    protected Object readPointer(PointerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        throw new IOException("Unexpected pointer");
    }

    @Override
    protected AtomReader readerForAtom(AtomTypeInfo info) {
        var name = info.base().name();
        return switch (name) {
            case "bool" -> AtomReader.BOOL_8;
            case "wchar", "tchar" -> AtomReader.CHAR_16;
            case "uint8", "int8" -> AtomReader.INT_8;
            case "uint16", "int16" -> AtomReader.INT_16;
            case "uint", "int", "uint32", "int32", "ucs4" -> AtomReader.INT_32;
            case "uint64", "int64", "uintptr" -> AtomReader.INT_64;
            case "HalfFloat" -> AtomReader.FLOAT_16;
            case "float" -> AtomReader.FLOAT_32;
            case "double" -> AtomReader.FLOAT_64;
            case "String" -> StringReader.INSTANCE;
            case "WString" -> WStringReader.INSTANCE;
            default -> throw new IllegalArgumentException("Unknown atom type: " + info.name() + " (" + name + ")");
        };
    }

    private Object readSimpleContainer(
        ContainerTypeInfo info,
        BinaryReader reader,
        TypeFactory factory
    ) throws IOException {
        var count = reader.readInt();
        var item = info.itemType();

        // Fast path
        if (item instanceof AtomTypeInfo atom) {
            return readerForAtom(atom).read(reader, count, info);
        }

        var result = info.newInstance(count);
        for (int i = 0; i < count; i++) {
            info.set(result, i, read(item, reader, factory));
        }

        return result;
    }

    private Object readHashContainer(
        ContainerTypeInfo info,
        BinaryReader reader,
        TypeFactory factory
    ) throws IOException {
        var count = reader.readInt();
        var item = info.itemType();
        var result = info.newInstance(count);

        for (int i = 0; i < count; i++) {
            // NOTE: Hash is based on the key - for HashMap, and on the value - for HashSet
            //       We don't actually need to store or use it - but we'll have to compute it
            //       when serialization support is added
            reader.skip(4); // hash
            info.set(result, i, read(item, reader, factory));
        }

        return result;
    }

    private static final class StringReader implements AtomReader {
        static final StringReader INSTANCE = new StringReader();

        @Override
        public Object read(BinaryReader reader) throws IOException {
            int length = reader.readInt();
            if (length == 0) {
                return "";
            }

            var hash = reader.readInt();
            var data = reader.readBytes(length);
            int actual = DecimaHash.crc32().hash(Bytes.wrap(data)).asInt() & 0x7fffffff;
            if (hash != actual) {
                throw new IllegalArgumentException("String is corrupted - mismatched checksum");
            }

            return new String(data, StandardCharsets.UTF_8);
        }
    }

    private static final class WStringReader implements AtomReader {
        static final WStringReader INSTANCE = new WStringReader();

        @Override
        public Object read(BinaryReader reader) throws IOException {
            return reader.readString(reader.readInt() * 2, StandardCharsets.UTF_16LE);
        }
    }
}
