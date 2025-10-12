package sh.adelessfox.odradek.rtti;

public sealed interface TypeInfo
    permits AtomTypeInfo, ClassTypeInfo, ContainerTypeInfo, EnumTypeInfo, PointerTypeInfo {

    String name();

    /**
     * A Java type that represents that type at runtime.
     */
    Class<?> type();

    default AtomTypeInfo asAtom() {
        if (this instanceof AtomTypeInfo i) {
            return i;
        }
        throw new IllegalStateException("Not an atom");
    }

    default ClassTypeInfo asClass() {
        if (this instanceof ClassTypeInfo i) {
            return i;
        }
        throw new IllegalStateException("Not a class");
    }

    default ContainerTypeInfo asContainer() {
        if (this instanceof ContainerTypeInfo i) {
            return i;
        }
        throw new IllegalStateException("Not a container");
    }

    default EnumTypeInfo asEnum() {
        if (this instanceof EnumTypeInfo i) {
            return i;
        }
        throw new IllegalStateException("Not an enum");
    }

    default PointerTypeInfo asPointer() {
        if (this instanceof PointerTypeInfo i) {
            return i;
        }
        throw new IllegalStateException("Not a pointer");
    }
}
