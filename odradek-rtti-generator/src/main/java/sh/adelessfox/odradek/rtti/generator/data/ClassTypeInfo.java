package sh.adelessfox.odradek.rtti.generator.data;

import sh.adelessfox.odradek.rtti.factory.TypeName;

import java.util.List;
import java.util.Set;

public record ClassTypeInfo(
    String name,
    List<ClassBaseInfo> bases,
    List<ClassAttrInfo> attrs,
    Set<String> messages,
    int version,
    int flags
) implements TypeInfo {
    public boolean isAssignableTo(String name) {
        if (name().equals(name)) {
            return true;
        }
        for (ClassBaseInfo base : bases) {
            if (base.type().isAssignableTo(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TypeName typeName() {
        return TypeName.of(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
