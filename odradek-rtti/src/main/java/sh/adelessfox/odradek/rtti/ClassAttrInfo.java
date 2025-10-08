package sh.adelessfox.odradek.rtti;

import java.util.Optional;

public interface ClassAttrInfo {
    // ERTTIAttrFlags
    int ATTR_DONT_SERIALIZE_BINARY = 2;

    String name();

    Optional<String> group();

    TypeInfo type();

    Optional<String> min();

    Optional<String> max();

    int offset();

    int flags();

    boolean isProperty();

    default boolean isSerialized() {
        return (flags() & ATTR_DONT_SERIALIZE_BINARY) == 0;
    }
}
