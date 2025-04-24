package sh.adelessfox.odradek.rtti.generator.data;

import sh.adelessfox.odradek.rtti.factory.TypeName;

public record PointerTypeInfo(
    String name,
    TypeInfoRef type
) implements TypeInfo {
    @Override
    public TypeName typeName() {
        return TypeName.of(name, type.typeName());
    }

    @Override
    public String toString() {
        return typeName().toString();
    }
}
