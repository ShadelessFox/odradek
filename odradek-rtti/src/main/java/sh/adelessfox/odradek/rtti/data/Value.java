package sh.adelessfox.odradek.rtti.data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public sealed interface Value<T extends Enum<T>>
    permits Value.OfEnum, Value.OfEnumSet {

    non-sealed interface OfEnum<T extends Enum<T>> extends Value<T> {
        @SuppressWarnings("unchecked")
        default T unwrap() {
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        default Optional<T> tryUnwrap() {
            return Optional.of((T) this);
        }
    }

    non-sealed interface OfEnumSet<T extends Enum<T> & Value<T>> extends Value<T> {
        default boolean contains(T t) {
            return (value() & t.value()) == t.value();
        }
    }

    static <T extends Enum<T> & OfEnum<T>> OfEnum<T> valueOf(Class<T> enumClass, int value) {
        for (T constant : enumClass.getEnumConstants()) {
            if (constant.value() == value) {
                return constant;
            }
        }
        return new OfConst<>(value);
    }

    static <T extends Enum<T> & OfEnumSet<T>> OfEnumSet<T> setOf(Class<T> enumClass, int value) {
        var values = new HashSet<Value<T>>();
        for (T constant : enumClass.getEnumConstants()) {
            if ((constant.value() & value) != 0) {
                value &= ~constant.value();
                values.add(constant);
            }
        }
        if (value != 0) {
            values.add(new OfConst<>(value));
        }
        return new OfSet<>(Set.copyOf(values));
    }

    int value();

    record OfConst<T extends Enum<T> & Value<T>>(int value) implements OfEnum<T> {
        @Override
        public T unwrap() {
            throw new IllegalStateException("Value " + value + " does not correspond to any known constant");
        }

        @Override
        public Optional<T> tryUnwrap() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    record OfSet<T extends Enum<T> & OfEnumSet<T>>(Set<Value<T>> values) implements OfEnumSet<T> {
        @Override
        public boolean contains(T t) {
            return values.contains(t);
        }

        @Override
        public int value() {
            return values.stream()
                .mapToInt(Value::value)
                .reduce(0, (a, b) -> a | b);
        }

        @Override
        public String toString() {
            return values.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(" | "));
        }
    }
}
