package sh.adelessfox.odradek.rtti;

public non-sealed interface PointerTypeInfo extends TypeInfo {
    String pointerType();

    TypeInfo itemType();
}
