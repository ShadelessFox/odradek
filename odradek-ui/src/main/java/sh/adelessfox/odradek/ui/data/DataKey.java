package sh.adelessfox.odradek.ui.data;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A typed, optionally qualified by a name, key for data retrieval via {@link DataContext}.
 *
 * @see DataContext#lookup(DataKey)
 */
public final class DataKey<T> {
    private final Type type;
    private final String name;

    private DataKey(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public static <T> DataKey<T> of(Class<T> type) {
        return new DataKey<>(type, null);
    }

    public static <T> DataKey<List<T>> listOf(Class<T> elementType) {
        return new DataKey<>(TypeToken.getParameterized(List.class, elementType).getType(), null);
    }

    public DataKey<T> named(String name) {
        return new DataKey<>(type, Objects.requireNonNull(name, "name"));
    }

    public Type type() {
        return type;
    }

    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof DataKey<?> other && type.equals(other.type) && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return "DataKey[" + type.getTypeName() + (name == null ? "" : ", name=" + name) + "]";
    }
}
