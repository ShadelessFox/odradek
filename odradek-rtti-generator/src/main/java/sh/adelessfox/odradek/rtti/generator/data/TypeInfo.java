package sh.adelessfox.odradek.rtti.generator.data;

import sh.adelessfox.odradek.rtti.factory.TypeName;

public sealed interface TypeInfo
    permits AtomTypeInfo, ClassTypeInfo, EnumTypeInfo, ContainerTypeInfo, PointerTypeInfo {

    TypeName typeName();

    default TypeInfoRef ref() {
        return new TypeInfoRef() {
            @Override
            public TypeName typeName() {
                return TypeInfo.this.typeName();
            }

            @Override
            public TypeInfo value() {
                return TypeInfo.this;
            }
        };
    }
}
