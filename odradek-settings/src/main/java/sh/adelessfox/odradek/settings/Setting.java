package sh.adelessfox.odradek.settings;

/**
 * Represents a settings with an associated value. The value can be retrieved, set, and reset to its default value.
 *
 * @param <T> the type of the value
 */
public interface Setting<T> {
    /**
     * Retrieves the current value of the setting.
     *
     * @return the current value of the setting
     */
    T value();

    /**
     * Sets the value of the setting.
     *
     * @param value the new value to set
     */
    void set(T value);

    /**
     * Resets the setting to its default value, specified by {@link SettingsKey}
     */
    void reset();
}
