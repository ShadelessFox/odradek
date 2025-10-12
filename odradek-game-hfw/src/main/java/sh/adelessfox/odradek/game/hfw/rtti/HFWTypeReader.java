package sh.adelessfox.odradek.game.hfw.rtti;

import sh.adelessfox.odradek.hashing.HashFunction;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.rtti.io.AbstractTypeReader;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;

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
    protected void readAtomHandle(AtomTypeInfo info, BinaryReader reader, TypeFactory factory, Object object, VarHandle handle) throws IOException {
        getAtomReader(info).readSingle(reader, object, handle);
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
        var name = info.base().name();
        return switch (name) {
            case "bool" -> AtomReader.BOOL;
            case "wchar", "tchar" -> AtomReader.CHAR;
            case "uint8", "int8" -> AtomReader.BYTE;
            case "uint16", "int16" -> AtomReader.SHORT;
            case "uint", "int", "uint32", "int32", "ucs4" -> AtomReader.INT;
            case "uint64", "int64", "uintptr" -> AtomReader.LONG;
            case "HalfFloat" -> AtomReader.HALF;
            case "float" -> AtomReader.FLOAT;
            case "double" -> AtomReader.DOUBLE;
            case "String" -> AtomReader.STRING;
            case "WString" -> AtomReader.WSTRING;
            default -> throw new IllegalArgumentException("Unknown atom type: " + info.name() + " (" + name + ")");
        };
    }

    private sealed interface AtomReader {
        AtomReader BYTE = of((r, o, h) -> h.set(o, r.readByte()), BinaryReader::readByte, BinaryReader::readBytes);
        AtomReader SHORT = of((r, o, h) -> h.set(o, r.readShort()), BinaryReader::readShort, BinaryReader::readShorts);
        AtomReader INT = of((r, o, h) -> h.set(o, r.readInt()), BinaryReader::readInt, BinaryReader::readInts);
        AtomReader LONG = of((r, o, h) -> h.set(o, r.readLong()), BinaryReader::readLong, BinaryReader::readLongs);
        AtomReader HALF = of((r, o, h) -> h.set(o, r.readHalf()), BinaryReader::readHalf, BinaryReader::readHalfs);
        AtomReader FLOAT = of((r, o, h) -> h.set(o, r.readFloat()), BinaryReader::readFloat, BinaryReader::readFloats);
        AtomReader DOUBLE = of((r, o, h) -> h.set(o, r.readDouble()), BinaryReader::readDouble, BinaryReader::readDoubles);
        AtomReader CHAR = of(r -> (char) r.readShort(), (r, o, h) -> h.set(o, (char) r.readShort()));
        AtomReader BOOL = of(BinaryReader::readByteBoolean, (r, o, h) -> h.set(o, r.readByteBoolean()));
        AtomReader STRING = of(HFWTypeReader::readString, (r, o, h) -> h.set(o, readString(r)));
        AtomReader WSTRING = of(HFWTypeReader::readWString, (r, o, h) -> h.set(o, readWString(r)));

        @FunctionalInterface
        interface ReadSingleHandle {
            void read(BinaryReader reader, Object object, VarHandle handle) throws IOException;
        }

        @FunctionalInterface
        interface ReadSingle {
            Object read(BinaryReader reader) throws IOException;
        }

        @FunctionalInterface
        interface ReadArray {
            Object read(BinaryReader reader, int count) throws IOException;
        }

        static AtomReader of(ReadSingleHandle readSingleHandle, ReadSingle readSingle, ReadArray readArray) {
            return new Single(readSingleHandle, readSingle, readArray);
        }

        static AtomReader of(ReadSingle readSingle, ReadSingleHandle readSingleHandle) {
            return new SingleMultiple(readSingleHandle, readSingle);
        }

        void readSingle(BinaryReader reader, Object object, VarHandle handle) throws IOException;

        Object readSingle(BinaryReader reader) throws IOException;

        Object readMultiple(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException;

        record Single(
            ReadSingleHandle readSingleHandle,
            ReadSingle readSingle,
            ReadArray readArray
        ) implements AtomReader {
            @Override
            public Object readSingle(BinaryReader reader) throws IOException {
                return readSingle.read(reader);
            }

            @Override
            public void readSingle(BinaryReader reader, Object object, VarHandle handle) throws IOException {
                readSingleHandle.read(reader, object, handle);
            }

            @Override
            public Object readMultiple(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                assert info.type().isArray();
                return readArray.read(reader, count);
            }
        }

        record SingleMultiple(
            ReadSingleHandle readSingleHandle,
            ReadSingle readSingle
        ) implements AtomReader {
            @Override
            public Object readSingle(BinaryReader reader) throws IOException {
                return readSingle.read(reader);
            }

            @Override
            public void readSingle(BinaryReader reader, Object object, VarHandle handle) throws IOException {
                readSingleHandle.read(reader, object, handle);
            }

            @Override
            public Object readMultiple(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                Object result = info.newInstance(count);
                for (int i = 0; i < count; i++) {
                    info.set(result, i, readSingle(reader));
                }
                return result;
            }
        }
    }
}
