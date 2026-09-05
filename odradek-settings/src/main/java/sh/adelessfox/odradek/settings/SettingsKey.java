package sh.adelessfox.odradek.settings;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class SettingsKey<T> {
    private final String name;
    private final Type type;
    private final Supplier<? extends T> defaultSupplier;

    private SettingsKey(String name, Type type, Supplier<? extends T> defaultSupplier) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.defaultSupplier = Objects.requireNonNull(defaultSupplier, "defaultSupplier");
    }

    public static <T> SettingsKey<T> of(String name, Class<T> type, Supplier<? extends T> defaultSupplier) {
        return new SettingsKey<>(name, type, defaultSupplier);
    }

    public static <T> SettingsKey<Optional<T>> optionalOf(
        String name,
        Class<T> type
    ) {
        return new SettingsKey<>(name, TypeToken.getParameterized(Optional.class, type).getType(), Optional::empty);
    }

    public static <T> SettingsKey<List<T>> listOf(
        String name,
        Class<T> type,
        Supplier<? extends List<T>> defaultSupplier
    ) {
        return new SettingsKey<>(name, TypeToken.getParameterized(List.class, type).getType(), defaultSupplier);
    }

    public String name() {
        return name;
    }

    public Type type() {
        return type;
    }

    T createDefault() {
        return Objects.requireNonNull(defaultSupplier.get(), "null default value for settings key '" + name + "'");
    }

    @Override
    public String toString() {
        return "SettingsKey[name=" + name + ", type=" + type + "]";
    }
}
