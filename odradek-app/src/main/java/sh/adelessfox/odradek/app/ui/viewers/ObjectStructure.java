package sh.adelessfox.odradek.app.ui.viewers;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.ContainerTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;
import sh.adelessfox.odradek.util.Gatherers;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public sealed interface ObjectStructure
    extends TreeStructure<ObjectStructure>
    permits ObjectStructure.Group, ObjectStructure.Node {

    record Group(
        Game game,
        ClassTypeInfo info,
        String name,
        List<Attr> attrs,
        Object object
    ) implements ObjectStructure {
        @Override
        public boolean equals(Object o) {
            return o instanceof Group(_, var info1, var name1, _, var object1)
                && info.equals(info1)
                && name.equals(name1)
                && object == object1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(info, name, System.identityHashCode(object));
        }
    }

    sealed interface Node extends ObjectStructure {
        Game game();

        TypeInfo type();

        Object value();
    }

    record Compound(Game game, ClassTypeInfo type, Object object) implements Node {
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

    record Attr(Game game, ClassTypeInfo info, ClassAttrInfo attr, Object object) implements Node {
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

    record Index(Game game, ContainerTypeInfo info, Object object, int index) implements Node {
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

    @Override
    default List<? extends ObjectStructure> getChildren() {
        return switch (this) {
            case Group group -> group.attrs();
            case Node node when node.value() == null -> List.of();
            case Node node -> switch (node.type()) {
                case ClassTypeInfo c -> c.serializedAttrs().stream()
                    .map(attr -> new Attr(node.game(), c, attr, node.value()))
                    .gather(Gatherers.groupingBy(
                        attr -> attr.attr().group(),
                        LinkedHashMap::new,
                        Collectors.toList()))
                    .flatMap(e -> e.getKey().isEmpty()
                        ? e.getValue().stream()
                        : Stream.of(new Group(node.game(), c, e.getKey().orElseThrow(), e.getValue(), node.value())))
                    .sorted(Comparator.comparing(e -> e instanceof Group ? -1 : 1))
                    .toList();
                case ContainerTypeInfo c -> IntStream.range(0, c.length(node.value()))
                    .mapToObj(index -> new Index(node.game(), c, node.value(), index))
                    .toList();
                default -> throw new IllegalStateException();
            };
        };
    }

    @Override
    default boolean hasChildren() {
        return switch (this) {
            case Group _ -> true;
            case Node node when node.value() == null -> false;
            case Node node -> switch (node.type()) {
                case ClassTypeInfo _, ContainerTypeInfo _ -> true;
                default -> false;
            };
        };
    }
}
