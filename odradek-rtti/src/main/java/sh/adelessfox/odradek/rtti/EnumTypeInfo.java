package sh.adelessfox.odradek.rtti;

import sh.adelessfox.odradek.rtti.data.Value;

import java.util.List;

public non-sealed interface EnumTypeInfo extends TypeInfo {
    int size();

    List<EnumValueInfo> values();

    <T extends Enum<T> & Value.OfEnum<T>> Value.OfEnum<T> valueOf(int value);

    <T extends Enum<T> & Value.OfEnumSet<T>> Value.OfEnumSet<T> setOf(int value);
}
