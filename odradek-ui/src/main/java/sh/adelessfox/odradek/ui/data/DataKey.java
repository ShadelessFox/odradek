package sh.adelessfox.odradek.ui.data;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class DataKey<T> {
    private static final Map<String, DataKey<?>> index = new ConcurrentHashMap<>();

    private final String name;

    private DataKey(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public static <T> DataKey<T> create(String name) {
        Objects.requireNonNull(name, "name");
        return (DataKey<T>) index.computeIfAbsent(name, DataKey::new);
    }

    public boolean is(String name) {
        return this.name.equals(name);
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof DataKey<?> other && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "DataKey[" + name + "]";
    }
}
