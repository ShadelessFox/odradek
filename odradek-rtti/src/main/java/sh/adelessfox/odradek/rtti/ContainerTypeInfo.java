package sh.adelessfox.odradek.rtti;

public non-sealed interface ContainerTypeInfo extends TypeInfo {
    String containerType();

    TypeInfo itemType();

    int length(Object object);

    Object get(Object object, int index);

    void set(Object object, int index, Object value);
}
