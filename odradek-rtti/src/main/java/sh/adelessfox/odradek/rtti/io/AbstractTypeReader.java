package sh.adelessfox.odradek.rtti.io;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataHolder;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.rtti.runtime.*;

import java.io.IOException;

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
        Object object = info.newInstance();
        for (ClassAttrInfo attr : info.serializableAttrs()) {
            attr.set(object, read(attr.type().get(), reader, factory));
        }
        if (object instanceof ExtraBinaryDataHolder holder) {
            holder.deserialize(reader, factory);
        }
        return object;
    }

    protected abstract Object readAtom(AtomTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException;

    protected abstract Value<?> readEnum(EnumTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException;

    protected abstract Object readContainer(ContainerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException;

    protected abstract Ref<?> readPointer(PointerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException;
}
