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
import java.util.Map;

import static sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.RTTIRefObject;

public class HFWTypeReader extends AbstractTypeReader {
    private static final Map<String, AtomReader> READERS = Map.ofEntries(
        Map.entry("wchar", AtomReader.of(r -> (char) r.readShort())),
        Map.entry("tchar", AtomReader.of(r -> (char) r.readShort())),
        Map.entry("bool", AtomReader.of(BinaryReader::readByteBoolean)),
        Map.entry("uint8", AtomReader.of(BinaryReader::readByte, BinaryReader::readBytes)),
        Map.entry("int8", AtomReader.of(BinaryReader::readByte, BinaryReader::readBytes)),
        Map.entry("uint16", AtomReader.of(BinaryReader::readShort, BinaryReader::readShorts)),
        Map.entry("int16", AtomReader.of(BinaryReader::readShort, BinaryReader::readShorts)),
        Map.entry("uint", AtomReader.of(BinaryReader::readInt, BinaryReader::readInts)),
        Map.entry("int", AtomReader.of(BinaryReader::readInt, BinaryReader::readInts)),
        Map.entry("uint32", AtomReader.of(BinaryReader::readInt, BinaryReader::readInts)),
        Map.entry("int32", AtomReader.of(BinaryReader::readInt, BinaryReader::readInts)),
        Map.entry("ucs4", AtomReader.of(BinaryReader::readInt, BinaryReader::readInts)),
        Map.entry("uint64", AtomReader.of(BinaryReader::readLong, BinaryReader::readLongs)),
        Map.entry("int64", AtomReader.of(BinaryReader::readLong, BinaryReader::readLongs)),
        Map.entry("uintptr", AtomReader.of(BinaryReader::readLong, BinaryReader::readLongs)),
        Map.entry("HalfFloat", AtomReader.of(BinaryReader::readHalf, BinaryReader::readHalfs)),
        Map.entry("float", AtomReader.of(BinaryReader::readFloat, BinaryReader::readFloats)),
        Map.entry("double", AtomReader.of(BinaryReader::readDouble, BinaryReader::readDoubles)),
        Map.entry("String", AtomReader.of(HFWTypeReader::readString)),
        Map.entry("WString", AtomReader.of(HFWTypeReader::readWString))
    );

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
        return getAtomReader(info).readSingle(reader);
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
            return getAtomReader(atom).readMultiple(reader, count, info);
        }

        var result = info.newInstance(count);
        for (int i = 0; i < count; i++) {
            info.set(result, i, read(item, reader, factory));
        }

        return result;
    }

    // TODO: Use specialized type (Map, Set, etc.)
    private Object readHashContainer(ContainerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
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

    private static AtomReader getAtomReader(AtomTypeInfo info) {
        var name = info.base().isPresent() ? info.base().get().name() : info.name();
        var reader = READERS.get(name);
        if (reader == null) {
            throw new IllegalArgumentException("Unknown atom type: " + info.name());
        }
        return reader;
    }


    private interface AtomReader {
        @FunctionalInterface
        interface ReadSingle {
            Object read(BinaryReader reader) throws IOException;
        }

        @FunctionalInterface
        interface ReadArray {
            Object read(BinaryReader reader, int count) throws IOException;
        }

        static AtomReader of(ReadSingle readSingle, ReadArray readArray) {
            return new AtomReader() {
                @Override
                public Object readSingle(BinaryReader reader) throws IOException {
                    return readSingle.read(reader);
                }

                @Override
                public Object readMultiple(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                    return readArray.read(reader, count);
                }
            };
        }

        static AtomReader of(ReadSingle readSingle) {
            return new AtomReader() {
                @Override
                public Object readSingle(BinaryReader reader) throws IOException {
                    return readSingle.read(reader);
                }

                @Override
                public Object readMultiple(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                    Object result = info.newInstance(count);
                    for (int i = 0; i < count; i++) {
                        info.set(result, i, readSingle.read(reader));
                    }
                    return result;
                }
            };
        }

        Object readSingle(BinaryReader reader) throws IOException;

        Object readMultiple(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException;
    }
}
