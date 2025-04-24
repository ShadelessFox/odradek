package sh.adelessfox.odradek.rtti.runtime;

import sh.adelessfox.odradek.rtti.factory.TypeName;

public interface TypeInfoRef {
    TypeName name();

    TypeInfo get();
}
