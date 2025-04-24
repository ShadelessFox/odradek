package sh.adelessfox.odradek.rtti.generator.data;

import sh.adelessfox.odradek.rtti.factory.TypeName;

import java.util.List;

public record EnumTypeInfo(
    String name,
    List<EnumValueInfo> values,
    EnumValueSize size,
    boolean flags
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
