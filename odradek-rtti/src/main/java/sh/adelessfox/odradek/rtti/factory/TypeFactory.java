package sh.adelessfox.odradek.rtti.factory;

import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.util.Collection;

public interface TypeFactory {
    TypeInfo get(String name);

    TypeInfo get(TypeId id);

    Collection<TypeInfo> getAll();

    <T extends TypedObject> T newInstance(ClassTypeInfo info);

    default <T extends TypedObject> T newInstance(Class<T> clazz) {
        var name = clazz.getSimpleName();
        var info = get(name).asClass();
        return newInstance(info);
    }
}
