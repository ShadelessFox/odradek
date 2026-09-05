package sh.adelessfox.odradek.settings;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A key for a setting in the settings system that has a name and a type.
 * <p>
 * Keys should have unique names within one {@link Settings} instance. Attempt
 * to retrieve a key with the same name but a different type will result in an exception.
 * <p>
 * A key can either be:
 * <ul>
 * <li>Required: The setting must have a value of the specified type {@code T}. Its value cannot be {@code null}</li>
 * <li>Optional: The setting may have a value of the specified type {@code T}, or it may be absent. The default value is absent.</li>
 * <li>List: The setting may have a list of values of the specified type {@code T}. The default value is an empty list.</li>
 * </ul>
 *
 * @param <T> the type of the value associated with the setting
 */
public final class SettingsKey<T> {
    private final String name;
    private final Type type;
    private final Supplier<? extends T> defaultSupplier;

    private SettingsKey(String name, Type type, Supplier<? extends T> defaultSupplier) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.defaultSupplier = Objects.requireNonNull(defaultSupplier, "defaultSupplier");
    }

    /**
     * Creates a new required settings key with the specified name, type, and default value supplier.
     *
     * @param name            the name of the settings key
     * @param type            the type of the value associated with the settings key
     * @param defaultSupplier a supplier that provides the default value for the settings key
     * @param <T>             the type of the value associated with the settings key
     * @return a new instance of {@link SettingsKey} with the specified name, type, and default value supplier
     */
    public static <T> SettingsKey<T> of(String name, Class<T> type, Supplier<? extends T> defaultSupplier) {
        return new SettingsKey<>(name, type, defaultSupplier);
    }

    /**
     * Creates a new optional settings key with the specified name and type.
     * The default value for an optional key is always {@link Optional#empty()}.
     *
     * @param name the name of the settings key
     * @param type the type of the value associated with the settings key
     * @param <T>  the type of the value associated with the settings key
     * @return a new instance of {@link SettingsKey} with the specified name and type, and a default value of {@link Optional#empty()}
     */
    public static <T> SettingsKey<Optional<T>> optionalOf(String name, Class<T> type) {
        return new SettingsKey<>(name, TypeToken.getParameterized(Optional.class, type).getType(), Optional::empty);
    }

    /**
     * Creates a new list settings key with the specified name and type.
     * The default value for a list key is always an empty list.
     *
     * @param name the name of the settings key
     * @param type the type of the values in the list associated with the settings key
     * @param <T>  the type of the values in the list associated with the settings key
     * @return a new instance of {@link SettingsKey} with the specified name and type, and a default value of an empty list
     */
    public static <T> SettingsKey<List<T>> listOf(String name, Class<T> type) {
        return new SettingsKey<>(name, TypeToken.getParameterized(List.class, type).getType(), List::of);
    }

    /**
     * Returns the name of the settings key.
     *
     * @return the name of the settings key
     */
    public String name() {
        return name;
    }

    /**
     * Returns the type of the value associated with the settings key.
     *
     * @return the type of the value associated with the settings key
     */
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
