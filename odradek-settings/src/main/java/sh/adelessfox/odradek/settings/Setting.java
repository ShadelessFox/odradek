package sh.adelessfox.odradek.settings;

import java.util.function.Consumer;

public interface Setting<T> {
    T value();

    void set(T value);

    void reset();

    default void ifPresent(Consumer<? super T> action) {
        var value = value();
        if (value != null) {
            action.accept(value);
        }
    }

    default T orElse(T other) {
        var value = value();
        return value != null ? value : other;
    }
}
