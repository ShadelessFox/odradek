package sh.adelessfox.odradek.rtti.runtime;

import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeName;

public record EnumTypeInfo(
    TypeName.Simple name,
    Class<? extends Enum<?>> type,
    int size,
    boolean isSet
) implements TypeInfo {
    @SuppressWarnings("unchecked")
    public <T extends Enum<T> & Value.OfEnum<T>> Value.OfEnum<T> valueOf(int value) {
        if (isSet) {
            throw new IllegalStateException("Enum " + name + " is a set");
        }
        return Value.valueOf((Class<T>) type, value);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T> & Value.OfEnumSet<T>> Value.OfEnumSet<T> setOf(int value) {
        if (!isSet) {
            throw new IllegalStateException("Enum " + name + " is not a set");
        }
        return Value.setOf((Class<T>) type, value);
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
