package sh.adelessfox.odradek.game;

public interface ObjectWithIdHolder<T> extends ObjectIdHolder, ObjectHolder<T> {
    @Override
    default T get() {
        return object();
    }
}
