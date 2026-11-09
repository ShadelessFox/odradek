package sh.adelessfox.odradek.rtti.io;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataHolder;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.util.Booleans;
import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.math.BigInteger;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class AbstractTypeReader {
    private final Map<ClassTypeInfo, AttributeReader[]> decodingPlans = new IdentityHashMap<>();

    public Object read(TypeInfo info, BinarySource source, TypeFactory factory) throws IOException {
        return switch (info) {
            case AtomTypeInfo t -> readAtom(t, source, factory);
            case EnumTypeInfo t -> readEnum(t, source, factory);
            case ClassTypeInfo t -> readCompound(t, source, factory);
            case ContainerTypeInfo t -> readContainer(t, source, factory);
            case PointerTypeInfo t -> readPointer(t, source, factory);
            case BitSetTypeInfo _ -> throw new NotImplementedException(); // TODO
        };
    }

    protected TypedObject readCompound(
        ClassTypeInfo info,
        BinarySource source,
        TypeFactory factory
    ) throws IOException {
        var object = info.newInstance();
        fillCompound(info, source, factory, object);
        return object;
    }

    protected void fillCompound(
        ClassTypeInfo info,
        BinarySource source,
        TypeFactory factory,
        Object target
    ) throws IOException {
        for (AttributeReader attribute : getOrCreateDecodingPlan(info)) {
            attribute.read(source, factory, target);
        }
        if (target instanceof ExtraBinaryDataHolder holder) {
            holder.deserialize(source, factory);
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
                    yield (source, _, target) -> atomReader.read(source, target, handle);
                }
                case EnumTypeInfo t -> (source, factory, target) -> handle.set(target, readEnum(t, source, factory));
                case ClassTypeInfo t ->
                    (source, factory, target) -> handle.set(target, readCompound(t, source, factory));
                case ContainerTypeInfo t ->
                    (source, factory, target) -> handle.set(target, readContainer(t, source, factory));
                case PointerTypeInfo t ->
                    (source, factory, target) -> handle.set(target, readPointer(t, source, factory));
                case BitSetTypeInfo _ -> (_, _, _) -> {
                    throw new NotImplementedException(); // TODO
                };
            };
        }
        return plan;
    }

    @FunctionalInterface
    private interface AttributeReader {
        void read(BinarySource source, TypeFactory factory, Object target) throws IOException;
    }

    protected Object readAtom(AtomTypeInfo info, BinarySource source, TypeFactory factory) throws IOException {
        return readerForAtom(info).read(source);
    }

    protected Value<?> readEnum(EnumTypeInfo info, BinarySource source, TypeFactory factory) throws IOException {
        int value = switch (info.size()) {
            case Byte.BYTES -> source.readByte();
            case Short.BYTES -> source.readShort();
            case Integer.BYTES -> source.readInt();
            default -> throw new IllegalArgumentException("Unexpected enum size: " + info.size());
        };
        if (info instanceof EnumSetTypeInfo) {
            return info.setOf(value);
        } else {
            return info.valueOf(value);
        }
    }

    protected abstract Object readContainer(
        ContainerTypeInfo info,
        BinarySource source,
        TypeFactory factory
    ) throws IOException;

    protected abstract Object readPointer(
        PointerTypeInfo info,
        BinarySource source,
        TypeFactory factory
    ) throws IOException;

    protected abstract AtomReader readerForAtom(AtomTypeInfo info);

    protected interface AtomReader {
        AtomReader INT_8 = new Int8();
        AtomReader INT_16 = new Int16();
        AtomReader INT_32 = new Int32();
        AtomReader INT_64 = new Int64();
        AtomReader INT_128 = new Int128(true);
        AtomReader UINT_128 = new Int128(false);
        AtomReader FLOAT_16 = new Float16();
        AtomReader FLOAT_32 = new Float32();
        AtomReader FLOAT_64 = new Float64();
        AtomReader CHAR_16 = new Char16();
        AtomReader BOOL_8 = new Bool8();

        Object read(BinarySource source) throws IOException;

        default void read(BinarySource source, Object target, VarHandle handle) throws IOException {
            handle.set(target, read(source));
        }

        default Object read(BinarySource source, int count, ContainerTypeInfo info) throws IOException {
            var result = info.newInstance(count);
            for (int i = 0; i < count; i++) {
                info.set(result, i, read(source));
            }
            return result;
        }

        final class Int8 implements AtomReader {
            @Override
            public Object read(BinarySource source) throws IOException {
                return source.readByte();
            }

            @Override
            public void read(BinarySource source, Object target, VarHandle handle) throws IOException {
                handle.set(target, source.readByte());
            }

            @Override
            public Object read(BinarySource source, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == Bytes.class;
                return source.readBytes(count);
            }
        }

        final class Int16 implements AtomReader {
            @Override
            public Object read(BinarySource source) throws IOException {
                return source.readShort();
            }

            @Override
            public void read(BinarySource source, Object target, VarHandle handle) throws IOException {
                handle.set(target, source.readShort());
            }

            @Override
            public Object read(BinarySource source, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == Shorts.class;
                return source.readShorts(count);
            }
        }

        final class Int32 implements AtomReader {
            @Override
            public Object read(BinarySource source) throws IOException {
                return source.readInt();
            }

            @Override
            public void read(BinarySource source, Object target, VarHandle handle) throws IOException {
                handle.set(target, source.readInt());
            }

            @Override
            public Object read(BinarySource source, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == Ints.class;
                return source.readInts(count);
            }
        }

        final class Int64 implements AtomReader {
            @Override
            public Object read(BinarySource source) throws IOException {
                return source.readLong();
            }

            @Override
            public void read(BinarySource source, Object target, VarHandle handle) throws IOException {
                handle.set(target, source.readLong());
            }

            @Override
            public Object read(BinarySource source, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == Longs.class;
                return source.readLongs(count);
            }
        }

        final class Int128 implements AtomReader {
            private final boolean signed;

            public Int128(boolean signed) {
                this.signed = signed;
            }

            @Override
            public Object read(BinarySource source) throws IOException {
                var magnitude = source.readBytes(16).toArray();

                // The value is stored in little-endian order; BigInteger expects big-endian order
                for (int i = 0; i < 8; i++) {
                    byte tmp = magnitude[i];
                    magnitude[i] = magnitude[15 - i];
                    magnitude[15 - i] = tmp;
                }

                return signed ? new BigInteger(magnitude) : new BigInteger(1, magnitude);
            }
        }

        final class Float16 implements AtomReader {
            @Override
            public Object read(BinarySource source) throws IOException {
                return source.readHalf();
            }

            @Override
            public void read(BinarySource source, Object target, VarHandle handle) throws IOException {
                handle.set(target, source.readHalf());
            }

            @Override
            public Object read(BinarySource source, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == Floats.class;
                return source.readHalfs(count);
            }
        }

        final class Float32 implements AtomReader {
            @Override
            public Object read(BinarySource source) throws IOException {
                return source.readFloat();
            }

            @Override
            public void read(BinarySource source, Object target, VarHandle handle) throws IOException {
                handle.set(target, source.readFloat());
            }

            @Override
            public Object read(BinarySource source, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == Floats.class;
                return source.readFloats(count);
            }
        }

        final class Float64 implements AtomReader {
            @Override
            public void read(BinarySource source, Object target, VarHandle handle) throws IOException {
                handle.set(target, source.readDouble());
            }

            @Override
            public Object read(BinarySource source) throws IOException {
                return source.readDouble();
            }

            @Override
            public Object read(BinarySource source, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == Doubles.class;
                return source.readDoubles(count);
            }
        }

        final class Bool8 implements AtomReader {
            @Override
            public Object read(BinarySource source) throws IOException {
                return source.readBool(BoolFormat.BYTE);
            }

            @Override
            public void read(BinarySource source, Object target, VarHandle handle) throws IOException {
                handle.set(target, source.readBool(BoolFormat.BYTE));
            }

            @Override
            public Object read(BinarySource source, int count, ContainerTypeInfo info) throws IOException {
                assert info.type() == Booleans.class;
                return AtomReader.super.read(source, count, info);
            }
        }

        final class Char16 implements AtomReader {
            @Override
            public void read(BinarySource source, Object target, VarHandle handle) throws IOException {
                handle.set(target, (char) source.readShort());
            }

            @Override
            public Object read(BinarySource source) throws IOException {
                return (char) source.readShort();
            }

            @Override
            public Object read(BinarySource source, int count, ContainerTypeInfo info) throws IOException {
                throw new UnsupportedOperationException("Cannot read a container of char16 values");
            }
        }
    }
}
