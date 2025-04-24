package sh.adelessfox.odradek.rtti.generator.data;

import java.util.Optional;

public record ClassAttrInfo(
    String name,
    Optional<String> category,
    TypeInfoRef type,
    Optional<String> min,
    Optional<String> max,
    int position,
    int offset,
    int flags,
    boolean property
) {
}
