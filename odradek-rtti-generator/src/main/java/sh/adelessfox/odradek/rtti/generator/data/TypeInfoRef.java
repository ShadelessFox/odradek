package sh.adelessfox.odradek.rtti.generator.data;

import sh.adelessfox.odradek.rtti.factory.TypeName;

public interface TypeInfoRef {
    TypeName typeName();

    TypeInfo value();
}
