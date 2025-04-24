package sh.adelessfox.odradek.rtti.runtime;

import sh.adelessfox.odradek.rtti.factory.TypeName;

public record PointerTypeInfo(
    TypeName.Parameterized name,
    Class<?> type,
    TypeInfoRef itemType
) implements TypeInfo {
    @Override
    public String toString() {
        return name.toString();
    }
}
