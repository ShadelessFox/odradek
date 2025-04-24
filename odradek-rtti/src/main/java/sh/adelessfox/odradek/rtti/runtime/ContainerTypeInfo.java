package sh.adelessfox.odradek.rtti.runtime;

import sh.adelessfox.odradek.rtti.factory.TypeName;

import java.lang.invoke.MethodHandle;

public record ContainerTypeInfo(
    TypeName.Parameterized name,
    Class<?> type,
    TypeInfoRef itemType,
    MethodHandle getter,
    MethodHandle setter,
    MethodHandle length
) implements TypeInfo {
    @Override
    public String toString() {
        return name.toString();
    }

    public Object get(Object container, int index) {
        try {
            return getter.invoke(container, index);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to get element from " + name, e);
        }
    }

    public void set(Object container, int index, Object value) {
        try {
            setter.invoke(container, index, value);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to set element in " + name, e);
        }
    }

    public int length(Object container) {
        try {
            return (int) length.invoke(container);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to get length of " + name, e);
        }
    }
}
