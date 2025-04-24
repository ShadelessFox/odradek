package sh.adelessfox.odradek.app;

import sh.adelessfox.odradek.app.ui.tree.TreeStructure;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.runtime.*;

import java.util.List;
import java.util.stream.IntStream;

record ObjectStructure(
    ClassTypeInfo info,
    Object object
) implements TreeStructure<ObjectStructure.Element> {
    public sealed interface Element {
        TypeInfo type();

        Object value();

        record Class(
            ClassTypeInfo type,
            Object object
        ) implements Element {
            @Override
            public Object value() {
                return object;
            }

            @Override
            public String toString() {
                return getDisplayString(this);
            }
        }

        record Attr(
            Object object,
            ClassAttrInfo attr
        ) implements Element {
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
                return "%s = %s".formatted(attr.name(), getDisplayString(this));
            }
        }

        record Index(
            Object container,
            ContainerTypeInfo info,
            int index
        ) implements Element {
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
                return "[%d] = %s".formatted(index, getDisplayString(this));
            }
        }
    }

    @Override
    public Element getRoot() {
        return new Element.Class(info, object);
    }

    @Override
    public List<? extends Element> getChildren(Element parent) {
        var value = parent.value();
        if (value == null) {
            return List.of();
        }
        return getChildren(parent.type(), value);
    }

    @Override
    public boolean hasChildren(Element node) {
        var value = node.value();
        if (value == null) {
            return false;
        }
        return hasChildren(node.type(), value);
    }

    @Override
    public String toString() {
        return "ObjectStructure[%s]".formatted(info.name());
    }

    private List<? extends Element> getChildren(TypeInfo info, Object object) {
        return switch (info) {
            case ClassTypeInfo c -> c.displayableAttrs().stream()
                .map(attr -> new Element.Attr(object, attr))
                .toList();
            case ContainerTypeInfo c -> IntStream.range(0, c.length(object))
                .mapToObj(index -> new Element.Index(object, c, index))
                .toList();
            default -> throw new IllegalStateException();
        };
    }

    private boolean hasChildren(TypeInfo info, Object object) {
        return switch (info) {
            case ClassTypeInfo ignored -> true;
            case ContainerTypeInfo ignored -> true;
            default -> false;
        };
    }

    private static String getDisplayString(Element element) {
        var type = element.type().name().toString();
        var value = switch (element.type()) {
            case ClassTypeInfo ignored -> null;
            case ContainerTypeInfo container when element.value() != null ->
                "(%d items)".formatted(container.length(element.value()));
            case
                PointerTypeInfo ignored when element.value() instanceof Ref<?> ref && ref.get() instanceof TypedObject object ->
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
