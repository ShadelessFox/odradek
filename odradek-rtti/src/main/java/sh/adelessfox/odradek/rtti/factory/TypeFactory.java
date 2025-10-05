package sh.adelessfox.odradek.rtti.factory;

import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;

public interface TypeFactory {
    TypeInfo get(String name);

    TypeInfo get(TypeId id);

    <T> T newInstance(ClassTypeInfo info);

    default <T> T newInstance(String name) {
        return newInstance(get(name).asClass());
    }

    default <T> T newInstance(Class<T> clazz) {
        return newInstance(clazz.getSimpleName());
    }
}
