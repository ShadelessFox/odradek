package sh.adelessfox.odradek.rtti;

import java.util.Optional;

public interface ClassAttrInfo {
    String name();

    Optional<String> group();

    TypeInfo type();

    Optional<String> min();

    Optional<String> max();

    int offset();

    int flags();

    boolean property();
}
