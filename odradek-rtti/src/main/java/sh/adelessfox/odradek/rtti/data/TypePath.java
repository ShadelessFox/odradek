package sh.adelessfox.odradek.rtti.data;

import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.ContainerTypeInfo;

import java.util.ArrayList;
import java.util.List;

public record TypePath(List<Element> elements) {
    public sealed interface Element {
        record Attr(ClassTypeInfo type, ClassAttrInfo attr) implements Element {
        }

        record Index(ContainerTypeInfo type, int index) implements Element {
        }
    }

    public TypePath {
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        elements = List.copyOf(elements);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        var buf = new StringBuilder("o");
        for (Element element : elements) {
            switch (element) {
                case Element.Attr(_, var attr) -> buf.append('.').append(attr.name());
                case Element.Index(_, int index) -> buf.append('[').append(index).append(']');
            }
        }
        return buf.toString();
    }

    public static final class Builder {
        private final List<Element> elements = new ArrayList<>();

        private Builder() {
        }

        public void attr(ClassTypeInfo type, ClassAttrInfo attr) {
            elements.add(new Element.Attr(type, attr));
        }

        public void index(ContainerTypeInfo type, int index) {
            elements.add(new Element.Index(type, index));
        }

        public void pop() {
            elements.removeLast();
        }

        public TypePath build() {
            return new TypePath(elements);
        }
    }
}
