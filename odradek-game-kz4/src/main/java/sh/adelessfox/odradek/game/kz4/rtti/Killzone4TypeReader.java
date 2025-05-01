package sh.adelessfox.odradek.game.kz4.rtti;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.kz4.rtti.Killzone4.EPlatform;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.rtti.io.AbstractTypeReader;
import sh.adelessfox.odradek.rtti.runtime.AtomTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.ContainerTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.EnumTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.PointerTypeInfo;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Killzone4TypeReader extends AbstractTypeReader {
    private final List<Ref<?>> pointers = new ArrayList<>();
    private Header header;

    public static <T> T readCompound(Class<T> cls, BinaryReader reader, TypeFactory factory) throws IOException {
        return cls.cast(new Killzone4TypeReader().readCompound(factory.get(cls), reader, factory));
    }

    public List<Object> read(BinaryReader reader, TypeFactory factory) throws IOException {
        header = Header.read(reader);

        var typeInfoCount = reader.readInt();
        var typeInfo = reader.readObjects(typeInfoCount, RTTITypeInfo::read);
        var objectTypesCount = reader.readInt();
        var objectTypes = reader.readInts(objectTypesCount);
        var totalExplicitObjects = reader.readInt();
        var objectHeaders = reader.readObjects(objectTypesCount, ObjectHeader::read);

        var objects = new ArrayList<>(header.assetCount);
        for (int i = 0; i < objectTypes.length; i++) {
            var start = reader.position();

            var info = typeInfo.get(objectTypes[i]);
            var header = objectHeaders.get(i);
            var object = readCompound(factory.get(Killzone4TypeId.of(info.name)), reader, factory);

            var end = reader.position();
            if (header.size > 0 && end - start != header.size) {
                throw new IllegalStateException("Size mismatch for %s: %d (actual) != %d (expected)".formatted(info.name, end - start, header.size));
            }

            objects.add(object);
        }

        resolvePointers(objects);

        return objects;
    }

    private void resolvePointers(List<Object> objects) {
        for (Ref<?> pointer : pointers) {
            if (pointer instanceof LocalRef<?> localRef) {
                localRef.object = objects.get(localRef.index);
            }
        }

        pointers.clear();
    }

    @Override
    protected Object readContainer(ContainerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        var itemInfo = info.itemType().get();
        var itemType = itemInfo.type();
        var count = reader.readInt();

        // Fast path
        if (itemType == byte.class) {
            return reader.readBytes(count);
        } else if (itemType == short.class) {
            return reader.readShorts(count);
        } else if (itemType == int.class) {
            return reader.readInts(count);
        } else if (itemType == long.class) {
            return reader.readLongs(count);
        }

        // Slow path
        var array = Array.newInstance(itemType, count);
        for (int i = 0; i < count; i++) {
            Array.set(array, i, read(itemInfo, reader, factory));
        }

        if (info.type() == List.class) {
            return Arrays.asList((Object[]) array);
        } else {
            return array;
        }
    }

    @Override
    protected Value<?> readEnum(EnumTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        int value = switch (info.size()) {
            case Byte.BYTES -> reader.readByte();
            case Short.BYTES -> reader.readShort();
            case Integer.BYTES -> reader.readInt();
            default -> throw new IllegalArgumentException("Unexpected enum size: " + info.size());
        };
        if (info.isSet()) {
            return info.setOf(value);
        } else {
            return info.valueOf(value);
        }
    }

    @Override
    @SuppressWarnings("DuplicateBranchesInSwitch")
    protected Object readAtom(AtomTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        return switch (info.name().name()) {
            // Base types
            case "bool" -> reader.readByteBoolean();
            case "wchar" -> (char) reader.readShort();
            case "uint8", "int8" -> reader.readByte();
            case "uint16", "int16" -> reader.readShort();
            case "uint", "int", "uint32", "int32" -> reader.readInt();
            case "uint64", "int64" -> reader.readLong();
            case "uint128", "int128" -> new BigInteger(reader.readBytes(16));
            case "float" -> reader.readFloat();
            case "double" -> reader.readDouble();
            case "String" -> readString(reader);
            case "WString" -> readWString(reader);

            // Aliases
            case "RenderDataPriority", "MaterialType" -> reader.readShort();
            case "AnimationSet", "AnimationTagID", "PhysicsCollisionFilterInfo" -> reader.readInt();
            case "Filename" -> readString(reader);

            default -> throw new IllegalArgumentException("Unknown atom type: " + info.name());
        };
    }

    @Override
    protected Ref<?> readPointer(PointerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        var kind = reader.readByte();
        var pointer = switch (kind) {
            case 0 -> {
                int index = reader.readInt();
                if (index == 0) {
                    yield null;
                } else {
                    yield new LocalRef<>(index - 1);
                }
            }
            case 1 -> throw new IllegalStateException("External links are not supported");
            case 2 -> {
                var cacheHash = reader.readString(reader.readInt());
                var cachePath = reader.readString(reader.readInt());
                var sourceName = reader.readString(reader.readInt());
                var sourceType = reader.readString(reader.readInt());
                var sourcePath = reader.readString(reader.readInt());
                var sourceIndex = reader.readString(reader.readInt());
                yield new DependentRef<>(cacheHash, cachePath, sourceName, sourceType, sourcePath, sourceIndex);
            }
            default -> throw new NotImplementedException();
        };
        pointers.add(pointer);
        return pointer;
    }

    private String readString(BinaryReader reader) throws IOException {
        int index = Objects.checkIndex(reader.readInt(), header.stringCount);
        if (index == 0) {
            return "";
        }
        int length = reader.readInt();
        if (length == 0) {
            return "";
        }
        return reader.readString(length);
    }

    private String readWString(BinaryReader reader) throws IOException {
        int index = Objects.checkIndex(reader.readInt(), header.wideStringCount);
        if (index == 0) {
            return "";
        }
        int length = reader.readInt();
        if (length == 0) {
            return "";
        }
        return reader.readString(length * 2, StandardCharsets.UTF_16LE);
    }

    private record Header(
        int pointerMapSize,
        int stringCount,
        int wideStringCount,
        int assetCount
    ) {
        static Header read(BinaryReader reader) throws IOException {
            var version = reader.readString(14);
            if (!version.equals("RTTIBin<2.12> ")) {
                throw new IllegalStateException("Unsupported version: " + version);
            }
            var platform = EPlatform.valueOf(reader.readByte());
            if (platform != EPlatform.PINK) {
                throw new IllegalStateException("Unsupported platform: " + platform);
            }
            var endian = reader.readByte();
            if (endian != 0) {
                throw new IllegalStateException("Unsupported endian: " + endian);
            }
            var pointerMapSize = reader.readInt();
            var stringCount = reader.readInt();
            var wideStringCount = reader.readInt();
            var assetCount = reader.readInt();
            return new Header(pointerMapSize, stringCount, wideStringCount, assetCount);
        }
    }

    private record RTTITypeInfo(String name, byte[] hash) {
        static RTTITypeInfo read(BinaryReader reader) throws IOException {
            var name = reader.readString(reader.readInt());
            var hash = reader.readBytes(16);
            return new RTTITypeInfo(name, hash);
        }
    }

    private record ObjectHeader(byte[] hash, int size) {
        static ObjectHeader read(BinaryReader reader) throws IOException {
            var hash = reader.readBytes(16);
            var size = reader.readInt();
            return new ObjectHeader(hash, size);
        }
    }

    private static final class LocalRef<T> implements Ref<T> {
        private final int index;
        private Object object;

        private LocalRef(int index) {
            this.index = index;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get() {
            return (T) Objects.requireNonNull(object);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof LocalRef<?> that && index == that.index && object == that.object;
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public String toString() {
            return "<pointer to object at " + index + ">";
        }
    }

    private record DependentRef<T>(
        String cacheHash,
        String cachePath,
        String sourceName,
        String sourceType,
        String sourcePath,
        String sourceIndex
    ) implements Ref<T> {
        @Override
        public T get() {
            throw new NotImplementedException();
        }
    }
}
