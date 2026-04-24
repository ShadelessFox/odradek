package sh.adelessfox.odradek.rtti.util;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.Value;

public class SimpleTypeVisitor<P, E extends Throwable> implements TypeVisitor<P, E> {
    @Override
    public void visit(TypeInfo info, Object object, P p) throws E {
        switch (info) {
            case AtomTypeInfo i -> visitAtom(i, object, p);
            case ClassTypeInfo i -> visitClass(i, object, p);
            case ContainerTypeInfo i -> visitContainer(i, object, p);
            case EnumTypeInfo i -> visitEnum(i, (Value<?>) object, p);
            case PointerTypeInfo i -> visitPointer(i, object, p);
            case BitSetTypeInfo t -> throw new NotImplementedException(); // TODO;
        }
    }

    @Override
    public void visitAtom(AtomTypeInfo info, Object object, P p) throws E {
        // do nothing
    }

    @Override
    public void visitClass(ClassTypeInfo info, Object object, P p) throws E {
        for (ClassAttrInfo attr : info.serializedAttrs()) {
            var value = info.get(attr, object);
            if (value != null) {
                visitClassAttr(info, attr, value, p);
            }
        }
    }

    @Override
    public void visitClassAttr(ClassTypeInfo info, ClassAttrInfo attr, Object object, P p) throws E {
        visit(attr.type(), object, p);
    }

    @Override
    public void visitContainer(ContainerTypeInfo info, Object object, P p) throws E {
        for (int i = 0, length = info.length(object); i < length; i++) {
            var item = info.get(object, i);
            if (item != null) {
                visitContainerItem(info, item, i, p);
            }
        }
    }

    @Override
    public void visitContainerItem(ContainerTypeInfo info, Object object, int index, P p) throws E {
        visit(info.itemType(), object, p);
    }

    @Override
    public void visitEnum(EnumTypeInfo info, Value<?> object, P p) throws E {
        // do nothing
    }

    @Override
    public void visitPointer(PointerTypeInfo info, Object object, P p) throws E {
        // do nothing
    }
}
