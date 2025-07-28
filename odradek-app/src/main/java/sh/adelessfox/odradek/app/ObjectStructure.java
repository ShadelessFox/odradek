package sh.adelessfox.odradek.app;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.runtime.*;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.tree.TreeStructure;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public sealed interface ObjectStructure extends TreeStructure<ObjectStructure> {
    Game game();

    TypeInfo type();

    Object value();

    record Compound(Game game, ClassTypeInfo type, Object object) implements ObjectStructure {
        @Override
        public Object value() {
            return object;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Compound(_, var type1, var object1)
                && type.equals(type1)
                && object == object1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, System.identityHashCode(object));
        }

        @Override
        public String toString() {
            return ObjectStructure.getDisplayString(this);
        }
    }

    record Attr(Game game, ClassAttrInfo attr, Object object) implements ObjectStructure {
        @Override
        public TypeInfo type() {
            return attr.type().get();
        }

        @Override
        public Object value() {
            return attr.get(object);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Attr(_, var attr1, var object1)
                && attr.equals(attr1)
                && object == object1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(attr, System.identityHashCode(object));
        }

        @Override
        public String toString() {
            return "%s = %s".formatted(attr.name(), ObjectStructure.getDisplayString(this));
        }
    }

    record Index(Game game, ContainerTypeInfo info, Object object, int index) implements ObjectStructure {
        @Override
        public TypeInfo type() {
            return info.itemType().get();
        }

        @Override
        public Object value() {
            return info.get(object, index);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Index(_, var info1, var object1, var index1)
                && info.equals(info1)
                && object == object1
                && index == index1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(info, System.identityHashCode(object), index);
        }

        @Override
        public String toString() {
            return "[%d] = %s".formatted(index, ObjectStructure.getDisplayString(this));
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
                .map(attr -> new Attr(game(), attr, value))
                .toList();
            case ContainerTypeInfo c -> IntStream.range(0, c.length(value))
                .mapToObj(index -> new Index(game(), c, value, index))
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

    static String getDisplayString(ObjectStructure structure) {
        return ObjectStructure.getValueString(structure)
            .map(v -> "{%s} %s".formatted(structure.type(), v))
            .orElseGet(() -> "{%s}".formatted(structure.type()));
    }

    static Optional<String> getValueString(ObjectStructure structure) {
        var renderer = Renderer.renderers(structure.type()).findFirst();

        if (renderer.isPresent()) {
            return renderer.flatMap(r -> r.text(structure.type(), structure.value(), structure.game()));
        } else if (structure.type() instanceof AtomTypeInfo || structure.type() instanceof EnumTypeInfo) {
            return Optional.of(String.valueOf(structure.value()));
        } else {
            // Other types don't deserve a toString representation unless provided explicitly
            return Optional.empty();
        }
    }
}
