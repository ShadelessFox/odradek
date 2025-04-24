package sh.adelessfox.odradek.rtti.runtime;

import sh.adelessfox.odradek.rtti.factory.TypeName;

import java.util.List;

public record ClassTypeInfo(
    TypeName.Simple name,
    Class<?> interfaceType,
    Class<?> instanceType,
    List<ClassBaseInfo> bases,
    List<ClassAttrInfo> displayableAttrs,
    List<ClassAttrInfo> serializableAttrs
) implements TypeInfo {
    @SuppressWarnings("deprecation")
    public Object newInstance() {
        try {
            return instanceType.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create instance of " + name, e);
        }
    }

    public boolean isInstanceOf(Class<? extends TypedObject> type) {
        return type.isAssignableFrom(interfaceType);
    }

    @Override
    public Class<?> type() {
        return interfaceType;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
