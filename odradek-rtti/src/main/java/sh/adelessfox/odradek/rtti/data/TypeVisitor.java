package sh.adelessfox.odradek.rtti.data;

import sh.adelessfox.odradek.rtti.*;

public class TypeVisitor {
    protected void visitAtom(AtomTypeInfo info, Object object, TypePath.Builder builder) {
        // do nothing
    }

    protected void visitClass(ClassTypeInfo info, Object object, TypePath.Builder builder) {
        for (ClassAttrInfo attr : info.orderedAttrs()) {
            builder.attr(info, attr);
            visit(attr.type(), info.get(attr, object), builder);
            builder.pop();
        }
    }

    protected void visitContainer(ContainerTypeInfo info, Object object, TypePath.Builder builder) {
        for (int i = 0, length = info.length(object); i < length; i++) {
            builder.index(info, i);
            visit(info.itemType(), info.get(object, i), builder);
            builder.pop();
        }
    }

    protected void visitEnum(EnumTypeInfo info, Value<?> object, TypePath.Builder builder) {
        // do nothing
    }

    protected void visitPointer(PointerTypeInfo info, Object object, TypePath.Builder builder) {
        // do nothing
    }

    public void visit(TypeInfo info, Object object) {
        visit(info, object, TypePath.builder());
    }

    public void visit(TypeInfo info, Object object, TypePath.Builder builder) {
        if (object == null) {
            return;
        }
        switch (info) {
            case AtomTypeInfo i -> visitAtom(i, object, builder);
            case ClassTypeInfo i -> visitClass(i, object, builder);
            case ContainerTypeInfo i -> visitContainer(i, object, builder);
            case EnumTypeInfo i -> visitEnum(i, (Value<?>) object, builder);
            case PointerTypeInfo i -> visitPointer(i, object, builder);
        }
    }
}
