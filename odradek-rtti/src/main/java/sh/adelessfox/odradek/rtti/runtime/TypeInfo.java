package sh.adelessfox.odradek.rtti.runtime;

import sh.adelessfox.odradek.rtti.factory.TypeName;

public sealed interface TypeInfo
    permits AtomTypeInfo, ClassTypeInfo, ContainerTypeInfo, EnumTypeInfo, PointerTypeInfo {

    TypeName name();

    Class<?> type();
}
