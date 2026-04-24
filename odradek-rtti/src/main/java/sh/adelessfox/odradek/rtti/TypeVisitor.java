package sh.adelessfox.odradek.rtti;

import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.data.Value;

public interface TypeVisitor<P, E extends Throwable> {
    default void visit(TypedObject object, P p) throws E {
        visit(object.getType(), object, p);
    }

    void visit(TypeInfo info, Object object, P p) throws E;

    void visitAtom(AtomTypeInfo info, Object object, P p) throws E;

    void visitClass(ClassTypeInfo info, Object object, P p) throws E;

    void visitClassAttr(ClassTypeInfo info, ClassAttrInfo attr, Object object, P p) throws E;

    void visitContainer(ContainerTypeInfo info, Object object, P p) throws E;

    void visitContainerItem(ContainerTypeInfo info, Object object, int index, P p) throws E;

    void visitEnum(EnumTypeInfo info, Value<?> object, P p) throws E;

    void visitPointer(PointerTypeInfo info, Object object, P p) throws E;
}
