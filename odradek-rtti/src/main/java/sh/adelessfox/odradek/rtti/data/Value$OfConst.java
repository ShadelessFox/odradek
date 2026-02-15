package sh.adelessfox.odradek.rtti.data;

import java.util.Optional;

record Value$OfConst<T extends Enum<T> & Value<T>>(int value) implements Value.OfEnum<T> {
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
