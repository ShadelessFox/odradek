package sh.adelessfox.odradek.rtti;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public non-sealed interface ClassTypeInfo extends TypeInfo {
    List<ClassBaseInfo> bases();

    List<ClassAttrInfo> attrs();

    List<ClassAttrInfo> orderedAttrs();

    List<String> messages();

    int version();

    int flags();

    VarHandle handle(ClassAttrInfo attr);

    default Object get(ClassAttrInfo attr, Object object) {
        return handle(attr).get(object);
    }

    default void set(ClassAttrInfo attr, Object object, Object value) {
        handle(attr).set(object, value);
    }

    default List<ClassAttrInfo> serializedAttrs() {
        var attrs = new ArrayList<ClassAttrInfo>(attrs().size());
        for (ClassBaseInfo base : bases()) {
            attrs.addAll(base.type().serializedAttrs());
        }
        for (ClassAttrInfo attr : attrs()) {
            // NOTE: We can display non-serializable attributes in case they have property getters
            if (attr.isSerialized() && !attr.isProperty()) {
                attrs.add(attr);
            }
        }
        return List.copyOf(attrs);
    }

    default Optional<ClassAttrInfo> find(String name, Optional<String> group) {
        for (ClassAttrInfo attr : attrs()) {
            if (attr.name().equals(name) && attr.group().equals(group)) {
                return Optional.of(attr);
            }
        }
        for (ClassBaseInfo base : bases()) {
            var attr = base.type().find(name, group);
            if (attr.isPresent()) {
                return attr;
            }
        }
        return Optional.empty();
    }

    default ClassAttrInfo findOrThrow(String name, Optional<String> group) {
        return find(name, group).orElseThrow(() -> group
            .map(s -> new NoSuchElementException("Can't find attribute '" + name + "' in group '" + s + "'"))
            .orElseGet(() -> new NoSuchElementException("Can't find attribute '" + name + "'")));
    }

    default boolean isAssignableFrom(ClassTypeInfo other) {
        if (name().equals(other.name())) {
            return true;
        }
        for (ClassBaseInfo base : other.bases()) {
            if (isAssignableFrom(base.type())) {
                return true;
            }
        }
        return false;
    }
}
