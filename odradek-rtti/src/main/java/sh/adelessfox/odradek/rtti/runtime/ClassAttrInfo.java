package sh.adelessfox.odradek.rtti.runtime;

import java.lang.invoke.VarHandle;
import java.util.Optional;

public record ClassAttrInfo(
    String name,
    Optional<String> category,
    TypeInfoRef type,
    VarHandle handle,
    int offset,
    int flags
) {
    public Object get(Object object) {
        return handle.get(object);
    }

    public void set(Object object, Object value) {
        handle.set(object, value);
    }
}
