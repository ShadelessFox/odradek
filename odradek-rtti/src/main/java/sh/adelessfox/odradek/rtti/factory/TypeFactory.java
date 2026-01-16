package sh.adelessfox.odradek.rtti.factory;

import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;

import java.util.Collection;

public interface TypeFactory {
    TypeInfo get(String name);

    TypeInfo get(TypeId id);

    Collection<TypeInfo> getAll();

    <T> T newInstance(ClassTypeInfo info);

    default <T> T newInstance(Class<T> clazz) {
        var name = clazz.getSimpleName();
        var info = get(name).asClass();
        return newInstance(info);
    }
}
