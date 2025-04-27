package sh.adelessfox.odradek.app;

import sh.adelessfox.odradek.app.ui.tree.TreeStructure;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.runtime.*;

import java.util.List;
import java.util.stream.IntStream;

public sealed interface ObjectStructure extends TreeStructure<ObjectStructure> {
    TypeInfo type();

    Object value();

    record Compound(ClassTypeInfo type, Object object) implements ObjectStructure {
        @Override
        public Object value() {
            return object;
        }

        @Override
        public String toString() {
            return ObjectStructure.toString(this);
        }
    }

    record Attr(
        Object object,
        ClassAttrInfo attr
    ) implements ObjectStructure {
        @Override
        public TypeInfo type() {
            return attr.type().get();
        }

        @Override
        public Object value() {
            return attr.get(object);
        }

        @Override
        public String toString() {
            return "%s = %s".formatted(attr.name(), ObjectStructure.toString(this));
        }
    }

    record Index(Object container, ContainerTypeInfo info, int index) implements ObjectStructure {
        @Override
        public TypeInfo type() {
            return info.itemType().get();
        }

        @Override
        public Object value() {
            return info.get(container, index);
        }

        @Override
        public String toString() {
            return "[%d] = %s".formatted(index, ObjectStructure.toString(this));
        }
    }

    @Override
    default ObjectStructure getRoot() {
        return this;
    }

    default List<? extends ObjectStructure> getChildren(ObjectStructure element) {
        var value = element.value();
        if (value == null) {
            return List.of();
        }
        return switch (element.type()) {
            case ClassTypeInfo c -> c.displayableAttrs().stream()
                .map(attr -> new Attr(value, attr))
                .toList();
            case ContainerTypeInfo c -> IntStream.range(0, c.length(value))
                .mapToObj(index -> new Index(value, c, index))
                .toList();
            default -> throw new IllegalStateException();
        };
    }

    @Override
    default boolean hasChildren(ObjectStructure node) {
        var value = node.value();
        if (value == null) {
            return false;
        }
        return switch (node.type()) {
            case ClassTypeInfo _, ContainerTypeInfo _ -> true;
            default -> false;
        };
    }

    static String toString(ObjectStructure element) {
        var type = element.type().name().toString();
        var value = switch (element.type()) {
            case ClassTypeInfo _ -> null;
            case ContainerTypeInfo container when element.value() != null ->
                "(%d items)".formatted(container.length(element.value()));
            case
                PointerTypeInfo _ when element.value() instanceof Ref<?> ref && ref.get() instanceof TypedObject object ->
                "<%s>".formatted(object.getType().name());
            default -> String.valueOf(element.value());
        };
        if (value != null) {
            return "{%s} %s".formatted(type, value);
        } else {
            return "{%s}".formatted(type);
        }
    }
}
