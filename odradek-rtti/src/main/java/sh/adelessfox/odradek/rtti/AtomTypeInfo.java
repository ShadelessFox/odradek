package sh.adelessfox.odradek.rtti;

import java.util.Optional;

public non-sealed interface AtomTypeInfo extends TypeInfo {
    Optional<AtomTypeInfo> base();
}
