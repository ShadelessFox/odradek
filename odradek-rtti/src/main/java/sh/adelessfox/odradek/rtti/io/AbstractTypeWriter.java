package sh.adelessfox.odradek.rtti.io;

import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataHolder;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.io.IOException;

public abstract class AbstractTypeWriter {
    public final void write(TypedObject object, BinaryWriter writer) throws IOException {
        write(object, object.getType(), writer);
    }

    public final void write(Object object, TypeInfo info, BinaryWriter writer) throws IOException {
        switch (info) {
            case AtomTypeInfo i -> writeAtom(object, i, writer);
            case ClassTypeInfo i -> writeClass(object, i, writer);
            case ContainerTypeInfo i -> writeContainer(object, i, writer);
            case EnumTypeInfo i -> writeEnum(object, i, writer);
            case PointerTypeInfo i -> writePointer(object, i, writer);
        }
    }

    protected abstract void writeAtom(
        Object object,
        AtomTypeInfo info,
        BinaryWriter writer
    ) throws IOException;

    protected void writeClass(
        Object object,
        ClassTypeInfo info,
        BinaryWriter writer
    ) throws IOException {
        for (ClassAttrInfo attr : info.orderedAttrs()) {
            write(info.get(attr, object), attr.type(), writer);
        }
        if (object instanceof ExtraBinaryDataHolder holder) {
            holder.serialize(writer);
        }
    }

    protected abstract void writeContainer(
        Object object,
        ContainerTypeInfo info,
        BinaryWriter writer
    ) throws IOException;

    protected abstract void writeEnum(
        Object object,
        EnumTypeInfo info,
        BinaryWriter writer
    ) throws IOException;

    protected abstract void writePointer(
        Object object,
        PointerTypeInfo info,
        BinaryWriter writer
    ) throws IOException;
}
