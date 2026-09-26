package sh.adelessfox.odradek.rtti.io;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.io.BoolFormat;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataHolder;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class AbstractTypeReader {
    private final Map<ClassTypeInfo, AttributeReader[]> decodingPlans = new IdentityHashMap<>();

    public Object read(TypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        return switch (info) {
            case AtomTypeInfo t -> readAtom(t, reader, factory);
            case EnumTypeInfo t -> readEnum(t, reader, factory);
            case ClassTypeInfo t -> readCompound(t, reader, factory);
            case ContainerTypeInfo t -> readContainer(t, reader, factory);
            case PointerTypeInfo t -> readPointer(t, reader, factory);
            case BitSetTypeInfo _ -> throw new NotImplementedException(); // TODO
        };
    }

    protected TypedObject readCompound(
        ClassTypeInfo info,
        BinaryReader reader,
        TypeFactory factory
    ) throws IOException {
        var object = info.newInstance();
        fillCompound(info, reader, factory, object);
        return object;
    }

    protected void fillCompound(
        ClassTypeInfo info,
        BinaryReader reader,
        TypeFactory factory,
        Object target
    ) throws IOException {
        for (AttributeReader attribute : getOrCreateDecodingPlan(info)) {
            attribute.read(reader, factory, target);
        }
        if (target instanceof ExtraBinaryDataHolder holder) {
            holder.deserialize(reader, factory);
        }
    }

    private AttributeReader[] getOrCreateDecodingPlan(ClassTypeInfo info) {
        return decodingPlans.computeIfAbsent(info, this::createDecodingPlan);
    }

    private AttributeReader[] createDecodingPlan(ClassTypeInfo info) {
        var attrs = info.orderedAttrs();
        var plan = new AttributeReader[attrs.size()];
        for (int i = 0; i < attrs.size(); i++) {
            var attr = attrs.get(i);
            var handle = info.handle(attr);
            plan[i] = switch (attr.type()) {
                case AtomTypeInfo t -> {
                    var atomReader = readerForAtom(t);
                    yield (reader, _, target) -> atomReader.read(reader, target, handle);
                }
                case EnumTypeInfo t -> (reader, factory, target) -> handle.set(target, readEnum(t, reader, factory));
                case ClassTypeInfo t ->
                    (reader, factory, target) -> handle.set(target, readCompound(t, reader, factory));
                case ContainerTypeInfo t ->
                    (reader, factory, target) -> handle.set(target, readContainer(t, reader, factory));
                case PointerTypeInfo t ->
                    (reader, factory, target) -> handle.set(target, readPointer(t, reader, factory));
                case BitSetTypeInfo _ -> (_, _, _) -> {
                    throw new NotImplementedException(); // TODO
                };
            };
        }
        return plan;
    }

    @FunctionalInterface
    private interface AttributeReader {
        void read(BinaryReader reader, TypeFactory factory, Object target) throws IOException;
    }

    protected Object readAtom(AtomTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        return readerForAtom(info).read(reader);
    }

    protected abstract Value<?> readEnum(
        EnumTypeInfo info,
        BinaryReader reader,
        TypeFactory factory
    ) throws IOException;

    protected abstract Object readContainer(
        ContainerTypeInfo info,
        BinaryReader reader,
        TypeFactory factory
    ) throws IOException;

    protected abstract Object readPointer(
        PointerTypeInfo info,
        BinaryReader reader,
        TypeFactory factory
    ) throws IOException;

    protected abstract AtomReader readerForAtom(AtomTypeInfo info);

    protected interface AtomReader {
        AtomReader INT_8 = new Int8();
        AtomReader INT_16 = new Int16();
        AtomReader INT_32 = new Int32();
        AtomReader INT_64 = new Int64();
        AtomReader FLOAT_16 = new Float16();
        AtomReader FLOAT_32 = new Float32();
        AtomReader FLOAT_64 = new Float64();
        AtomReader CHAR_16 = new Char16();
        AtomReader BOOL_8 = new Bool8();

        Object read(BinaryReader reader) throws IOException;

        default void read(BinaryReader reader, Object target, VarHandle handle) throws IOException {
            handle.set(target, read(reader));
        }

        default Object read(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
            var result = info.newInstance(count);
            for (int i = 0; i < count; i++) {
                info.set(result, i, read(reader));
            }
            return result;
        }

        final class Int8 implements AtomReader {
            @Override
            public Object read(BinaryReader reader) throws IOException {
                return reader.readByte();
            }

            @Override
            public void read(BinaryReader reader, Object target, VarHandle handle) throws IOException {
                handle.set(target, reader.readByte());
            }

            @Override
            public Object read(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == byte[].class;
                return reader.readBytes(count);
            }
        }

        final class Int16 implements AtomReader {
            @Override
            public Object read(BinaryReader reader) throws IOException {
                return reader.readShort();
            }

            @Override
            public void read(BinaryReader reader, Object target, VarHandle handle) throws IOException {
                handle.set(target, reader.readShort());
            }

            @Override
            public Object read(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == short[].class;
                return reader.readShorts(count);
            }
        }

        final class Int32 implements AtomReader {
            @Override
            public Object read(BinaryReader reader) throws IOException {
                return reader.readInt();
            }

            @Override
            public void read(BinaryReader reader, Object target, VarHandle handle) throws IOException {
                handle.set(target, reader.readInt());
            }

            @Override
            public Object read(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == int[].class;
                return reader.readInts(count);
            }
        }

        final class Int64 implements AtomReader {
            @Override
            public Object read(BinaryReader reader) throws IOException {
                return reader.readLong();
            }

            @Override
            public void read(BinaryReader reader, Object target, VarHandle handle) throws IOException {
                handle.set(target, reader.readLong());
            }

            @Override
            public Object read(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == long[].class;
                return reader.readLongs(count);
            }
        }

        final class Float16 implements AtomReader {
            @Override
            public Object read(BinaryReader reader) throws IOException {
                return reader.readHalf();
            }

            @Override
            public void read(BinaryReader reader, Object target, VarHandle handle) throws IOException {
                handle.set(target, reader.readHalf());
            }

            @Override
            public Object read(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == float[].class;
                return reader.readHalfs(count);
            }
        }

        final class Float32 implements AtomReader {
            @Override
            public Object read(BinaryReader reader) throws IOException {
                return reader.readFloat();
            }

            @Override
            public void read(BinaryReader reader, Object target, VarHandle handle) throws IOException {
                handle.set(target, reader.readFloat());
            }

            @Override
            public Object read(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == float[].class;
                return reader.readFloats(count);
            }
        }

        final class Float64 implements AtomReader {
            @Override
            public void read(BinaryReader reader, Object target, VarHandle handle) throws IOException {
                handle.set(target, reader.readDouble());
            }

            @Override
            public Object read(BinaryReader reader) throws IOException {
                return reader.readDouble();
            }

            @Override
            public Object read(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == double[].class;
                return reader.readDoubles(count);
            }
        }

        final class Bool8 implements AtomReader {
            @Override
            public Object read(BinaryReader reader) throws IOException {
                return reader.readBool(BoolFormat.BYTE);
            }

            @Override
            public void read(BinaryReader reader, Object target, VarHandle handle) throws IOException {
                handle.set(target, reader.readBool(BoolFormat.BYTE));
            }

            @Override
            public Object read(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == boolean[].class;
                return AtomReader.super.read(reader, count, info);
            }
        }

        final class Char16 implements AtomReader {
            @Override
            public void read(BinaryReader reader, Object target, VarHandle handle) throws IOException {
                handle.set(target, (char) reader.readShort());
            }

            @Override
            public Object read(BinaryReader reader) throws IOException {
                return (char) reader.readShort();
            }

            @Override
            public Object read(BinaryReader reader, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == char[].class;
                return AtomReader.super.read(reader, count, info);
            }
        }
    }
}
