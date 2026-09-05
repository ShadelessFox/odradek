package sh.adelessfox.odradek.settings;

public interface Settings {
    <T> Setting<T> get(SettingsKey<T> key);
}
