package sh.adelessfox.odradek.rtti.io;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.io.BoolFormat;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataHolder;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.data.Value;

import java.io.IOException;
import java.lang.invoke.VarHandle;

public abstract class AbstractTypeWriter {
    public void write(Object object, TypeInfo info, BinaryWriter writer) throws IOException {
        switch (info) {
            case AtomTypeInfo t -> writeAtom(object, t, writer);
            case EnumTypeInfo t -> writeEnum((Value<?>) object, t, writer);
            case ClassTypeInfo t -> writeCompound((TypedObject) object, t, writer);
            case ContainerTypeInfo t -> writeContainer(object, t, writer);
            case PointerTypeInfo t -> writePointer(object, t, writer);
            case BitSetTypeInfo _ -> throw new NotImplementedException(); // TODO
            default -> throw new AssertionError();
        }
    }

    protected void writeCompound(
        TypedObject object,
        ClassTypeInfo info,
        BinaryWriter writer
    ) throws IOException {
        for (ClassAttrInfo attr : info.orderedAttrs()) {
            if (attr.type() instanceof AtomTypeInfo atom) {
                // Fast path to avoid boxing overhead for primitive types
                writerForAtom(atom).write(writer, object, info.handle(attr));
            } else {
                write(info.get(attr, object), attr.type(), writer);
            }
        }
        if (object instanceof ExtraBinaryDataHolder holder) {
            holder.serialize(writer);
        }
    }

    protected void writeAtom(Object object, AtomTypeInfo info, BinaryWriter writer) throws IOException {
        writerForAtom(info).write(writer, object);
    }

    protected abstract void writeEnum(
        Value<?> object,
        EnumTypeInfo info,
        BinaryWriter writer
    ) throws IOException;

    protected abstract void writeContainer(
        Object object,
        ContainerTypeInfo info,
        BinaryWriter writer
    ) throws IOException;

    protected abstract void writePointer(
        Object object,
        PointerTypeInfo info,
        BinaryWriter writer
    ) throws IOException;

    protected abstract AtomWriter writerForAtom(AtomTypeInfo info);

    protected interface AtomWriter {
        AtomWriter INT_8 = new Int8();
        AtomWriter INT_16 = new Int16();
        AtomWriter INT_32 = new Int32();
        AtomWriter INT_64 = new Int64();
        AtomWriter FLOAT_16 = new Float16();
        AtomWriter FLOAT_32 = new Float32();
        AtomWriter FLOAT_64 = new Float64();
        AtomWriter BOOL_8 = new Bool(BoolFormat.BYTE);

        void write(BinaryWriter writer, Object value) throws IOException;

        default void write(BinaryWriter writer, Object source, VarHandle handle) throws IOException {
            write(writer, handle.get(source));
        }

        default void write(BinaryWriter writer, Object value, ContainerTypeInfo info) throws IOException {
            int length = info.length(value);
            for (int i = 0; i < length; i++) {
                write(writer, info.get(value, i));
            }
        }

        final class Int8 implements AtomWriter {
            @Override
            public void write(BinaryWriter writer, Object value) throws IOException {
                writer.writeByte((byte) value);
            }

            @Override
            public void write(BinaryWriter writer, Object source, VarHandle handle) throws IOException {
                writer.writeByte((byte) handle.get(source));
            }

            @Override
            public void write(BinaryWriter writer, Object value, ContainerTypeInfo info) throws IOException {
                writer.writeBytes((byte[]) value);
            }
        }

        final class Int16 implements AtomWriter {
            @Override
            public void write(BinaryWriter writer, Object value) throws IOException {
                writer.writeShort((short) value);
            }

            @Override
            public void write(BinaryWriter writer, Object source, VarHandle handle) throws IOException {
                writer.writeShort((short) handle.get(source));
            }

            @Override
            public void write(BinaryWriter writer, Object value, ContainerTypeInfo info) throws IOException {
                writer.writeShorts((short[]) value);
            }
        }

        final class Int32 implements AtomWriter {
            @Override
            public void write(BinaryWriter writer, Object value) throws IOException {
                writer.writeInt((int) value);
            }

            @Override
            public void write(BinaryWriter writer, Object source, VarHandle handle) throws IOException {
                writer.writeInt((int) handle.get(source));
            }

            @Override
            public void write(BinaryWriter writer, Object value, ContainerTypeInfo info) throws IOException {
                writer.writeInts((int[]) value);
            }
        }

        final class Int64 implements AtomWriter {
            @Override
            public void write(BinaryWriter writer, Object value) throws IOException {
                writer.writeLong((long) value);
            }

            @Override
            public void write(BinaryWriter writer, Object source, VarHandle handle) throws IOException {
                writer.writeLong((long) handle.get(source));
            }

            @Override
            public void write(BinaryWriter writer, Object value, ContainerTypeInfo info) throws IOException {
                writer.writeLongs((long[]) value);
            }
        }

        final class Float16 implements AtomWriter {
            @Override
            public void write(BinaryWriter writer, Object value) throws IOException {
                writer.writeHalf((float) value);
            }

            @Override
            public void write(BinaryWriter writer, Object source, VarHandle handle) throws IOException {
                writer.writeHalf((float) handle.get(source));
            }

            @Override
            public void write(BinaryWriter writer, Object value, ContainerTypeInfo info) throws IOException {
                writer.writeHalfs((float[]) value);
            }
        }

        final class Float32 implements AtomWriter {
            @Override
            public void write(BinaryWriter writer, Object value) throws IOException {
                writer.writeFloat((float) value);
            }

            @Override
            public void write(BinaryWriter writer, Object source, VarHandle handle) throws IOException {
                writer.writeFloat((float) handle.get(source));
            }

            @Override
            public void write(BinaryWriter writer, Object value, ContainerTypeInfo info) throws IOException {
                writer.writeFloats((float[]) value);
            }
        }

        final class Float64 implements AtomWriter {
            @Override
            public void write(BinaryWriter writer, Object value) throws IOException {
                writer.writeDouble((double) value);
            }

            @Override
            public void write(BinaryWriter writer, Object source, VarHandle handle) throws IOException {
                writer.writeDouble((double) handle.get(source));
            }

            @Override
            public void write(BinaryWriter writer, Object value, ContainerTypeInfo info) throws IOException {
                writer.writeDoubles((double[]) value);
            }
        }

        record Bool(BoolFormat format) implements AtomWriter {
            @Override
            public void write(BinaryWriter writer, Object value) throws IOException {
                writer.writeBool((boolean) value, format);
            }

            @Override
            public void write(BinaryWriter writer, Object source, VarHandle handle) throws IOException {
                writer.writeBool((boolean) handle.get(source), format);
            }
        }
    }
}
