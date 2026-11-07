package sh.adelessfox.odradek.game.ds2.rtti;

import sh.adelessfox.odradek.game.decima.DecimaHash;
import sh.adelessfox.odradek.game.ds2.rtti.data.MotionMatchingVecN;
import sh.adelessfox.odradek.rtti.AtomTypeInfo;
import sh.adelessfox.odradek.rtti.ContainerTypeInfo;
import sh.adelessfox.odradek.rtti.PointerTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.rtti.io.AbstractTypeReader;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DS2TypeReader extends AbstractTypeReader {
    public static <T> T readCompound(Class<T> cls, BinarySource source, TypeFactory factory) throws IOException {
        var type = factory.get(cls.getSimpleName()).asClass();
        return cls.cast(new DS2TypeReader().readCompound(type, source, factory));
    }

    public TypedObject readObject(BinarySource source, TypeFactory factory) throws IOException {
        var hash = source.readLong();
        var size = source.readInt();
        var type = factory.get(DS2TypeId.of(hash)).asClass();

        var start = source.position();
        var object = readCompound(type, source, factory);
        var end = source.position();

        if (end - start != size) {
            throw new IllegalStateException(
                "Size mismatch for %s: %d (actual) != %d (expected)".formatted(type.name(), end - start, size));
        }

        int numLinks = source.readInt();
        if (numLinks != 0) {
            throw new IllegalStateException("Expected 0 links, got " + numLinks);
        }

        return object;
    }

    @Override
    protected Object readContainer(
        ContainerTypeInfo info,
        BinarySource source,
        TypeFactory factory
    ) throws IOException {
        return switch (info.containerType()) {
            case "HashMap", "HashSet" -> readHashContainer(info, source, factory);
            default -> readSimpleContainer(info, source, factory);
        };
    }

    @Override
    protected Object readPointer(PointerTypeInfo info, BinarySource source, TypeFactory factory) throws IOException {
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
            case "uint128" -> AtomReader.UINT_128;
            case "HalfFloat" -> AtomReader.FLOAT_16;
            case "float" -> AtomReader.FLOAT_32;
            case "double" -> AtomReader.FLOAT_64;
            case "StringHash" -> StringHashReader.INSTANCE;
            case "String" -> StringReader.INSTANCE;
            case "WString" -> WStringReader.INSTANCE;
            case "MotionMatchingVecN" -> MotionMatchingVecNReader.INSTANCE;
            default -> throw new IllegalArgumentException("Unknown atom type: " + info.name() + " (" + name + ")");
        };
    }

    private Object readSimpleContainer(
        ContainerTypeInfo info,
        BinarySource source,
        TypeFactory factory
    ) throws IOException {
        var count = source.readInt();
        var item = info.itemType();

        // Fast path
        if (item instanceof AtomTypeInfo atom) {
            return readerForAtom(atom).read(source, count, info);
        }

        var result = info.newInstance(count);
        for (int i = 0; i < count; i++) {
            info.set(result, i, read(item, source, factory));
        }

        return result;
    }

    private Object readHashContainer(
        ContainerTypeInfo info,
        BinarySource source,
        TypeFactory factory
    ) throws IOException {
        var count = source.readInt();
        var item = info.itemType();
        var result = info.newInstance(count);

        for (int i = 0; i < count; i++) {
            // NOTE: Hash is based on the key - for HashMap, and on the value - for HashSet
            //       We don't actually need to store or use it - but we'll have to compute it
            //       when serialization support is added
            source.skip(4); // hash
            info.set(result, i, read(item, source, factory));
        }

        return result;
    }

    private static final class StringHashReader implements AtomReader {
        static final StringHashReader INSTANCE = new StringHashReader();

        @Override
        public Object read(BinarySource source) throws IOException {
            int size = source.readInt();
            if (size != 4) {
                throw new IllegalArgumentException("Unexpected string hash size: " + size);
            }
            return source.readInt();
        }
    }

    private static final class StringReader implements AtomReader {
        static final StringReader INSTANCE = new StringReader();

        @Override
        public Object read(BinarySource source) throws IOException {
            int length = source.readInt();
            if (length == 0) {
                return "";
            }

            var hash = source.readInt();
            var data = source.readBytes(length);
            int actual = DecimaHash.crc32().hash(data).asInt() & 0x7fffffff;
            if (hash != actual) {
                throw new IllegalArgumentException("String is corrupted - mismatched checksum");
            }

            return data.toString(StandardCharsets.UTF_8);
        }
    }

    private static final class WStringReader implements AtomReader {
        static final WStringReader INSTANCE = new WStringReader();

        @Override
        public Object read(BinarySource source) throws IOException {
            return source.readString(source.readInt() * 2, StandardCharsets.UTF_16LE);
        }
    }

    private static final class MotionMatchingVecNReader implements AtomReader {
        static final MotionMatchingVecNReader INSTANCE = new MotionMatchingVecNReader();

        @Override
        public Object read(BinarySource source) throws IOException {
            return new MotionMatchingVecN(source.readFloats(72));
        }
    }
}
