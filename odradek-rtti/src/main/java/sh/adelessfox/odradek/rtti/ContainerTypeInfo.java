package sh.adelessfox.odradek.rtti;

public non-sealed interface ContainerTypeInfo extends TypeInfo {
    String containerType();

    TypeInfo itemType();

    Object newInstance(int length);

    Object get(Object object, int index);

    void set(Object object, int index, Object value);

    int length(Object object);
}
