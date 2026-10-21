package sh.adelessfox.odradek.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A registry of stable and computed values, optionally inheriting another context.
 * Local bindings shadow inherited bindings even when their current value is empty.
 * <p>
 * Can be subclassed to provide additional lookup behavior by overriding {@link #lookup(DataKey)}.
 *
 * @see DataKey
 */
public class DataContext {
    private final Map<DataKey<?>, Supplier<?>> bindings = new HashMap<>();
    private final DataContext parent;

    public DataContext() {
        parent = null;
    }

    public DataContext(DataContext parent) {
        this.parent = Objects.requireNonNull(parent, "parent");
    }

    /** Registers a non-null stable value, replacing any existing local binding. */
    public final <T> void put(DataKey<T> key, T value) {
        Objects.requireNonNull(value, "value");
        compute(key, () -> value);
    }

    /**
     * Registers a provider evaluated on every lookup, replacing any existing local binding.
     * <p>
     * A {@code null} result means empty, not missing; it masks the parent
     * value. Providers should be side-effect-free. Exceptions propagate to the caller.
     */
    public final <T> void compute(DataKey<T> key, Supplier<? extends T> provider) {
        bindings.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(provider, "provider"));
    }

    /** Returns the value, or empty for either an empty binding or a missing key. */
    public final <T> Optional<T> get(DataKey<T> key) {
        return lookup(key).optional();
    }

    public final <T, R> Optional<R> get(DataKey<T> key, Class<R> type) {
        return get(key).filter(type::isInstance).map(type::cast);
    }

    public final <T> T require(DataKey<T> key) {
        return get(key).orElseThrow(() -> new IllegalStateException("Data not found for key: " + key));
    }

    public final boolean has(DataKey<?> key) {
        return get(key).isPresent();
    }

    public final boolean has(DataKey<?> key, Class<?> type) {
        return get(key, type).isPresent();
    }

    @SuppressWarnings("unchecked")
    protected <T> Lookup<T> lookup(DataKey<T> key) {
        Objects.requireNonNull(key, "key");
        var provider = bindings.get(key);
        if (provider != null) {
            return Lookup.ofNullable((T) provider.get());
        }
        return parent == null ? Lookup.missing() : lookupIn(parent, key);
    }

    /** Allows subclasses in other packages to delegate without losing absence information. */
    protected final <T> Lookup<T> lookupIn(DataContext context, DataKey<T> key) {
        return context.lookup(key);
    }

    public sealed interface Lookup<T> {
        static <T> Lookup<T> missing() {
            return new Missing<>();
        }

        static <T> Lookup<T> empty() {
            return new Empty<>();
        }

        static <T> Lookup<T> ofNullable(T value) {
            return value == null ? empty() : new Present<>(value);
        }

        default boolean isMissing() {
            return this instanceof Missing<?>;
        }

        default Optional<T> optional() {
            return switch (this) {
                case Present<T>(var value) -> Optional.of(value);
                case Missing<T> _, Empty<T> _ -> Optional.empty();
            };
        }

        record Missing<T>() implements Lookup<T> {
        }

        record Empty<T>() implements Lookup<T> {
        }

        record Present<T>(T value) implements Lookup<T> {
            public Present {
                Objects.requireNonNull(value, "value");
            }
        }
    }
}
