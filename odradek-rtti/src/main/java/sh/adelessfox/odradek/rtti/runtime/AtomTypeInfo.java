package sh.adelessfox.odradek.rtti.runtime;

import sh.adelessfox.odradek.rtti.factory.TypeName;

public record AtomTypeInfo(
    TypeName.Simple name,
    Class<?> type
) implements TypeInfo {
    @Override
    public String toString() {
        return name.toString();
    }
}
