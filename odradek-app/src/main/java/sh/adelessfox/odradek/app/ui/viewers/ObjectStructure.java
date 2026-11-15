package sh.adelessfox.odradek.app.ui.viewers;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.ContainerTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public sealed interface ObjectStructure extends TreeStructure<ObjectStructure> {
    Game game();

    TypeInfo type();

    Object value();

    record Compound(
        Game game,
        ClassTypeInfo type,
        Object object
    ) implements ObjectStructure {
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

    record Attr(
        Game game,
        ClassTypeInfo info,
        ClassAttrInfo attr,
        Object object,
        Optional<String> group
    ) implements ObjectStructure {
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
            return o instanceof Attr(_, var clazz1, var attr1, var object1, _)
                && info.equals(clazz1)
                && attr.equals(attr1)
                && object == object1;
        }

        @Override
        public int hashCode() {
            return Objects.hash(attr, System.identityHashCode(object));
        }
    }

    record Index(
        Game game,
        ContainerTypeInfo info,
        Object object,
        int index
    ) implements ObjectStructure {
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
        if (value() == null) {
            return List.of();
        }
        return switch (type()) {
            case ClassTypeInfo c -> attributes(this, c);
            case ContainerTypeInfo c -> elements(this, c);
            default -> throw new IllegalStateException();
        };
    }

    @Override
    default boolean hasChildren() {
        if (value() == null) {
            return false;
        }
        return switch (type()) {
            case ClassTypeInfo _, ContainerTypeInfo _ -> true;
            default -> false;
        };
    }

    private static List<Attr> attributes(ObjectStructure node, ClassTypeInfo type) {
        var groups = type.serializedAttrs().stream().collect(Collectors.groupingBy(
            ClassAttrInfo::group,
            LinkedHashMap::new,
            Collectors.toList()));
        var showTitles = groups.keySet().stream().limit(2).count() > 1;
        return groups.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().isEmpty()))
            .flatMap(e -> IntStream.range(0, e.getValue().size()).mapToObj(i -> {
                var title = i == 0 && showTitles ? Optional.of(e.getKey().orElse("")) : Optional.<String>empty();
                return new Attr(node.game(), type, e.getValue().get(i), node.value(), title);
            }))
            .toList();
    }

    private static List<Index> elements(ObjectStructure node, ContainerTypeInfo info) {
        return IntStream.range(0, info.length(node.value()))
            .mapToObj(index -> new Index(node.game(), info, node.value(), index))
            .toList();
    }
}
