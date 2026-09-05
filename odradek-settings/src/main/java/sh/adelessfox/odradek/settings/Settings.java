package sh.adelessfox.odradek.settings;

/**
 * Represents a collection of settings that can be accessed using their corresponding {@link SettingsKey}.
 */
public interface Settings {
    /**
     * Retrieves the {@link Setting} associated with the specified {@link SettingsKey}.
     * <p>
     * When passing a key that does not exist in the settings,
     * a new setting will be created with the default value specified by the key.
     *
     * @param key the {@link SettingsKey} for which to retrieve the setting
     * @param <T> the type of the value associated with the setting
     * @return the {@link Setting} associated with the specified key
     */
    <T> Setting<T> get(SettingsKey<T> key);
}
