package sh.adelessfox.odradek.rtti.util;

import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.ContainerTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;

public class PathTypeVisitor<E extends Throwable> extends SimpleTypeVisitor<TypePath.Builder, E> {
    public void visit(TypedObject object) throws E {
        visit(object, TypePath.builder());
    }

    @Override
    public void visitClassAttr(
        ClassTypeInfo info,
        ClassAttrInfo attr,
        Object object,
        TypePath.Builder builder
    ) throws E {
        builder.attr(info, attr);
        try {
            super.visitClassAttr(info, attr, object, builder);
        } finally {
            builder.pop();
        }
    }

    @Override
    public void visitContainerItem(
        ContainerTypeInfo info,
        Object object,
        int index,
        TypePath.Builder builder
    ) throws E {
        builder.index(info, index);
        try {
            super.visitContainerItem(info, object, index, builder);
        } finally {
            builder.pop();
        }
    }
}
