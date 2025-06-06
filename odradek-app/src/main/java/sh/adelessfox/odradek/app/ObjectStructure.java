package sh.adelessfox.odradek.app;

import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.runtime.*;
import sh.adelessfox.odradek.ui.tree.TreeStructure;

import java.util.List;
import java.util.Objects;
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

        @Override
        public boolean equals(Object o) {
            return o instanceof Compound(var type1, var object1)
                && type.equals(type1)
                && object == object1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, System.identityHashCode(object));
        }
    }

    record Attr(
        ClassAttrInfo attr,
        Object object
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

        @Override
        public boolean equals(Object o) {
            return o instanceof Attr(var attr1, var object1)
                && attr.equals(attr1)
                && object == object1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(attr, System.identityHashCode(object));
        }
    }

    record Index(ContainerTypeInfo info, Object object, int index) implements ObjectStructure {
        @Override
        public TypeInfo type() {
            return info.itemType().get();
        }

        @Override
        public Object value() {
            return info.get(object, index);
        }

        @Override
        public String toString() {
            return "[%d] = %s".formatted(index, ObjectStructure.toString(this));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Index(var info1, var object1, var index1)
                && info.equals(info1)
                && object == object1
                && index == index1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(info, System.identityHashCode(object), index);
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
            // FIXME: Base attributes are not shown (e.g. ShaderFromFileResource -> ShaderResource)
            case ClassTypeInfo c -> c.displayableAttrs().stream()
                .map(attr -> new Attr(attr, value))
                .toList();
            case ContainerTypeInfo c -> IntStream.range(0, c.length(value))
                .mapToObj(index -> new Index(c, value, index))
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
            case ContainerTypeInfo container when element.value() != null -> "(%d items)".formatted(container.length(element.value()));
            case PointerTypeInfo _ when element.value() instanceof Ref<?> ref && ref.get() instanceof TypedObject object ->
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
