package sh.adelessfox.odradek.game.hfw.rtti;

import sh.adelessfox.odradek.hashing.HashFunction;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.rtti.io.AbstractTypeReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.RTTIRefObject;

public class HFWTypeReader extends AbstractTypeReader {
    public record ObjectInfo(ClassTypeInfo type, RTTIRefObject object) {
    }

    public static <T> T readCompound(Class<T> cls, BinaryReader reader, TypeFactory factory) throws IOException {
        var type = factory.get(cls.getSimpleName()).asClass();
        return cls.cast(new HFWTypeReader().readCompound(type, reader, factory));
    }

    public ObjectInfo readObject(BinaryReader reader, TypeFactory factory) throws IOException {
        var hash = reader.readLong();
        var size = reader.readInt();
        var type = factory.get(HFWTypeId.of(hash)).asClass();

        var start = reader.position();
        var object = readCompound(type, reader, factory);
        var end = reader.position();

        if (end - start != size) {
            throw new IllegalStateException("Size mismatch for %s: %d (actual) != %d (expected)".formatted(type.name(), end - start, size));
        }

        if (!(object instanceof RTTIRefObject refObject)) {
            throw new IllegalStateException("Expected RTTIRefObject, got " + type.name());
        }

        return new ObjectInfo(type, refObject);
    }

    @Override
    protected Object readAtom(AtomTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        var name = info.base().isPresent() ? info.base().get().name() : info.name();
        return switch (name) {
            // Simple types
            case "bool" -> reader.readByteBoolean();
            case "wchar", "tchar" -> (char) reader.readShort();
            case "uint8", "int8" -> reader.readByte();
            case "uint16", "int16" -> reader.readShort();
            case "uint", "int", "uint32", "int32", "ucs4" -> reader.readInt();
            case "uint64", "int64" -> reader.readLong();
            case "HalfFloat" -> Float.float16ToFloat(reader.readShort());
            case "float" -> reader.readFloat();
            case "double" -> reader.readDouble();

            // Dynamic types
            case "String" -> readString(reader);
            case "WString" -> readWString(reader);

            default -> throw new IllegalArgumentException("Unknown atom type: " + info.name());
        };
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
    protected Object readContainer(ContainerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        return switch (info.containerType()) {
            case "HashMap", "HashSet" -> readHashContainer(info, reader, factory);
            default -> readSimpleContainer(info, reader, factory);
        };
    }

    @Override
    protected Ref<?> readPointer(PointerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        throw new IOException("Unexpected pointer");
    }

    private Object readSimpleContainer(ContainerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        var count = reader.readInt();
        var item = info.itemType();

        // Fast path
        if (item instanceof AtomTypeInfo atom) {
            var name = atom.base().isPresent() ? atom.base().get().name() : atom.name();
            switch (name) {
                case "bool" -> {
                    var items = new boolean[count];
                    for (int i = 0; i < count; i++) {
                        items[i] = reader.readByteBoolean();
                    }
                    return items;
                }
                case "wchar", "tchar" -> {
                    var items = new char[count];
                    for (int i = 0; i < count; i++) {
                        items[i] = (char) reader.readShort();
                    }
                    return items;
                }
                case "uint8", "int8" -> {
                    return reader.readBytes(count);
                }
                case "uint16", "int16" -> {
                    return reader.readShorts(count);
                }
                case "uint", "int", "uint32", "int32", "ucs4" -> {
                    return reader.readInts(count);
                }
                case "uint64", "int64" -> {
                    return reader.readLongs(count);
                }
                case "HalfFloat" -> {
                    return reader.readHalfs(count);
                }
                case "float" -> {
                    return reader.readFloats(count);
                }
                case "double" -> {
                    return reader.readDoubles(count);
                }
            }
        }

        var items = new Object[count];
        for (int i = 0; i < count; i++) {
            items[i] = read(item, reader, factory);
        }

        return Arrays.asList(items);
    }

    private Object readHashContainer(ContainerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        var count = reader.readInt();
        var item = info.itemType();

        var items = new Object[count];
        for (int i = 0; i < count; i++) {
            // NOTE: Hash is based on the key - for HashMap, and on the value - for HashSet
            //       We don't actually need to store or use it - but we'll have to compute it
            //       when serialization support is added
            int hash = reader.readInt();
            items[i] = read(item, reader, factory);
        }

        // TODO: Use specialized type (Map, Set, etc.)
        return Arrays.asList(items);
    }

    private static String readString(BinaryReader reader) throws IOException {
        var length = reader.readInt();
        if (length == 0) {
            return "";
        }
        var hash = reader.readInt();
        var data = reader.readBytes(length);
        if (hash != HashFunction.crc32c().hash(data).asInt()) {
            throw new IllegalArgumentException("String is corrupted - mismatched checksum");
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    private static String readWString(BinaryReader reader) throws IOException {
        var length = reader.readInt();
        if (length == 0) {
            return "";
        }
        return reader.readString(length * 2, StandardCharsets.UTF_16LE);
    }
}
