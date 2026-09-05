package sh.adelessfox.odradek.settings;

public interface Setting<T> {
    T value();

    void set(T value);

    void reset();
}
