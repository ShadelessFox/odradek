package sh.adelessfox.odradek.app.ui.editors;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.ContainerTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public sealed interface ObjectStructure extends TreeStructure<ObjectStructure> {
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
    }

    record Attr(Game game, ClassTypeInfo info, ClassAttrInfo attr, Object object) implements ObjectStructure {
        @Override
        public TypeInfo type() {
            return attr.type();
        }

        @Override
        public Object value() {
            return info.get(attr, object);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Attr(_, var clazz1, var attr1, var object1)
                && info.equals(clazz1)
                && attr.equals(attr1)
                && object == object1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(attr, System.identityHashCode(object));
        }
    }

    record Index(Game game, ContainerTypeInfo info, Object object, int index) implements ObjectStructure {
        @Override
        public TypeInfo type() {
            return info.itemType();
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
    }

    Game game();

    TypeInfo type();

    Object value();

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
            case ClassTypeInfo c -> c.serializedAttrs().stream()
                .map(attr -> new Attr(game(), c, attr, value))
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
}
