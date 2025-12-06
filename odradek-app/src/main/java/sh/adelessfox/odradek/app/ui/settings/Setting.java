package sh.adelessfox.odradek.app.ui.settings;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Setting<T> {
    private T value;

    public Setting() {
        this.value = null;
    }

    public Setting(T value) {
        this.value = value;
    }

    public Optional<T> get() {
        return Optional.ofNullable(value);
    }

    public void set(T value) {
        this.value = value;
    }

    public void ifPresent(Consumer<? super T> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (value == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(value));
        }
    }

    public T compute(Supplier<? extends T> supplier) {
        if (value == null) {
            value = Objects.requireNonNull(supplier.get());
        }
        return value;
    }

    @Override
    public String toString() {
        return value != null ? "Setting[value=" + value + "]" : "Setting[]";
    }
}
