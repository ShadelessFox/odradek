package sh.adelessfox.odradek.app.ui.editors;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public sealed interface ObjectStructure extends TreeStructure<ObjectStructure> {
    Game game();

    TypeInfo type();

    Object value();

    default Optional<Consumer<StyledText.Builder>> keyTextBuilder() {
        Consumer<StyledText.Builder> consumer = switch (this) {
            case Attr x -> tb -> tb
                .add(x.attr().name(), StyledFragment.NAME)
                .add(" = ");
            case Index x -> tb -> tb
                .add("[" + x.index() + "]", StyledFragment.NAME)
                .add(" = ");
            default -> null;
        };
        return Optional.ofNullable(consumer);
    }

    default Optional<Consumer<StyledText.Builder>> valueTextBuilder(boolean allowStyledText) {
        if (this instanceof Compound) {
            return Optional.empty();
        }

        var value = value();
        if (value == null) {
            return Optional.of(tb -> tb.add("null", StyledFragment.GRAYED));
        }

        var type = type();
        var renderer = Renderer.renderer(type).orElse(null);

        if (renderer != null) {
            if (allowStyledText) {
                var styledText = renderer.styledText(type, value, game()).orElse(null);
                if (styledText != null) {
                    return Optional.of(tb -> tb.add(styledText));
                }
            }
            var text = renderer.text(type, value, game()).orElse(null);
            if (text != null) {
                return Optional.of(tb -> tb.add(text));
            }
            return Optional.empty();
        } else if (type instanceof AtomTypeInfo || type instanceof EnumTypeInfo) {
            // Special case for primitive values; could become a dedicated renderer later
            return Optional.of(tb -> tb.add(String.valueOf(value)));
        } else {
            // Other types don't deserve a toString representation unless provided explicitly
            return Optional.empty();
        }
    }

    default StyledText toStyledText() {
        var tb = StyledText.builder();
        var keyTextBuilder = keyTextBuilder().orElse(null);
        var valueTextBuilder = valueTextBuilder(true).orElse(null);

        if (keyTextBuilder != null) {
            keyTextBuilder.accept(tb);
        }

        if (valueTextBuilder != null) {
            tb.add("{" + type() + "} ", StyledFragment.GRAYED);
            valueTextBuilder.accept(tb);
        } else {
            tb.add("{" + type() + "}");
        }

        return tb.build();
    }

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
