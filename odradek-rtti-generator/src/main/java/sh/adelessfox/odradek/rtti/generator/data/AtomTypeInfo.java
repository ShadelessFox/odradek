package sh.adelessfox.odradek.rtti.generator.data;

import sh.adelessfox.odradek.rtti.factory.TypeName;

import java.util.Optional;

public record AtomTypeInfo(
    String name,
    Optional<TypeInfo> parent
) implements TypeInfo {
    @Override
    public TypeName typeName() {
        return TypeName.of(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
