package sh.adelessfox.odradek.rtti.io;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataHolder;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.lang.invoke.VarHandle;

public abstract class AbstractTypeReader {
    public Object read(TypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        return switch (info) {
            case AtomTypeInfo t -> readAtom(t, reader, factory);
            case EnumTypeInfo t -> readEnum(t, reader, factory);
            case ClassTypeInfo t -> readCompound(t, reader, factory);
            case ContainerTypeInfo t -> readContainer(t, reader, factory);
            case PointerTypeInfo t -> readPointer(t, reader, factory);
        };
    }

    protected Object readCompound(ClassTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        Object object = factory.newInstance(info);
        fillCompound(info, reader, factory, object);
        return object;
    }

    protected void fillCompound(ClassTypeInfo info, BinaryReader reader, TypeFactory factory, Object object) throws IOException {
        for (ClassAttrInfo attr : info.orderedAttrs()) {
            if (attr.type() instanceof AtomTypeInfo atom) {
                // TODO: Bypasses checks in get/set!
                readAtomHandle(atom, reader, factory, object, info.handle(attr));
            } else {
                info.set(attr, object, read(attr.type(), reader, factory));
            }
        }
        if (object instanceof ExtraBinaryDataHolder holder) {
            holder.deserialize(reader, factory);
        }
    }

    protected abstract void readAtomHandle(AtomTypeInfo info, BinaryReader reader, TypeFactory factory, Object object, VarHandle handle) throws IOException;

    protected abstract Object readAtom(AtomTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException;

    protected abstract Value<?> readEnum(EnumTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException;

    protected abstract Object readContainer(ContainerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException;

    protected abstract Ref<?> readPointer(PointerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException;
}
