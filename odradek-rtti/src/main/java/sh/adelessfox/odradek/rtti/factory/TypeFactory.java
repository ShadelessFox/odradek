package sh.adelessfox.odradek.rtti.factory;

import sh.adelessfox.odradek.rtti.runtime.ClassTypeInfo;

public interface TypeFactory {
    ClassTypeInfo get(TypeId id);

    ClassTypeInfo get(Class<?> cls);

    default <T> T newInstance(Class<T> cls) {
        return cls.cast(get(cls).newInstance());
    }
}
